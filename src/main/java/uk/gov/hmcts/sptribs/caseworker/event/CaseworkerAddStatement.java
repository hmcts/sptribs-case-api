package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.event.page.StatementSelectParty;
import uk.gov.hmcts.sptribs.caseworker.event.page.StatementUploadDocument;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.repositories.exception.document.DocumentSaveException;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentType;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.document.service.DocumentsService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_ADD_STATEMENT;
import static uk.gov.hmcts.sptribs.caseworker.util.MessageUtil.handleDocumentException;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
@RequiredArgsConstructor
public class CaseworkerAddStatement implements CCDConfig<CaseData, State, UserRole> {

    private static final StatementSelectParty statementSelectParty = new StatementSelectParty();
    private static final StatementUploadDocument statementUploadDocument = new StatementUploadDocument();

    private final DocumentsService documentsService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Event.EventBuilder<CaseData, UserRole, State> eventBuilder =
            configBuilder
                .event(CASEWORKER_ADD_STATEMENT)
                .forAllStates()
                .name("Add statement")
                .description("Add statement")
                .showSummary()
                .aboutToStartCallback(this::aboutToStart)
                .aboutToSubmitCallback(this::aboutToSubmit)
                .grant(CREATE_READ_UPDATE,
                    ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN)
                .grantHistoryOnly(ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_JUDGE, ST_CIC_SENIOR_JUDGE)
                .publishToCamunda();

        PageBuilder pageBuilder = new PageBuilder(eventBuilder);
        statementSelectParty.addTo(pageBuilder);
        statementUploadDocument.addTo(pageBuilder);
    }

    private AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {
        CaseData data = details.getData();
        CicCase cicCase = data.getCicCase();

        // Set up a partial document to hold the form state
        List<DynamicListElement> listItems = new ArrayList<>();

        if (cicCase.getFullName() != null && !cicCase.getFullName().isEmpty()) {
            listItems.add(DynamicListElement.builder().code(UUID.randomUUID()).label("Subject").build());
        }
        if (cicCase.getRepresentativeFullName() != null && !cicCase.getRepresentativeFullName().isEmpty()) {
            listItems.add(DynamicListElement.builder().code(UUID.randomUUID()).label("Representative").build());
        }
        if (cicCase.getRespondentName() != null && !cicCase.getRespondentName().isEmpty()) {
            listItems.add(DynamicListElement.builder().code(UUID.randomUUID()).label("Respondent").build());
        }

        data.getCicCase().setUploadPartyStatementParty(DynamicList.builder().listItems(listItems).build());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }

    private AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                        final CaseDetails<CaseData, State> beforeDetails) {
        CaseData data = details.getData();
        CicCase cicCase = data.getCicCase();
        List<String> errors = new ArrayList<>();

        // Convert the form data to a CaseworkerCICDocument
        DynamicList party = cicCase.getUploadPartyStatementParty();
        Document uploadedDocument = cicCase.getUploadPartyStatementDocument();

        if (party == null || party.getValue() == null || uploadedDocument == null) {
            errors.add("Party statement did not have all required fields");
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(data)
                .errors(errors)
                .build();
        }

        CaseworkerCICDocument document = CaseworkerCICDocument.builder()
            .documentLink(uploadedDocument)
            .documentCategory(DocumentType.PARTY_STATEMENT)
            .documentEmailContent("Statement from " + party.getValue().getLabel())
            .date(LocalDate.now(Clock.systemDefaultZone()))
            .build();
        document.getDocumentLink().setCategoryId(DocumentType.PARTY_STATEMENT.getCategory());

        // Add document to where it should be located
        if (cicCase.getStatementDocuments() == null) {
            cicCase.setStatementDocuments(new ArrayList<>());
        }

        List<ListValue<CaseworkerCICDocument>> partyStatements = cicCase.getStatementDocuments();
        ListValue<CaseworkerCICDocument> newDocuments = ListValue.<CaseworkerCICDocument>builder()
            .id(String.valueOf(partyStatements.size()))
            .value(document)
            .build();
        partyStatements.addFirst(newDocuments);

        try {
            documentsService.buildAndSaveNewDocumentEntity(
                document.getDocumentLink(),
                details.getId(),
                document.getDocumentCategory(),
                CaseDocumentType.DOCUMENT_MANAGEMENT
            );
        } catch (DocumentSaveException e) {
            log.error("Error while saving document for statement upload", e);
            errors.add(handleDocumentException(document.getDocumentLink(), e.getMessage()));
        }

        // Clear out temporary form data
        cicCase.setUploadPartyStatementParty(null);
        cicCase.setUploadPartyStatementDocument(null);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
}
