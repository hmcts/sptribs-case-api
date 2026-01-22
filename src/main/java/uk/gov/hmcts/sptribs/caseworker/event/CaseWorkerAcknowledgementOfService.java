package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.sptribs.caseworker.event.page.PartyStatementSelect;
import uk.gov.hmcts.sptribs.caseworker.event.page.UploadPartyStatementDoc;
import uk.gov.hmcts.sptribs.caseworker.service.StatementService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.model.AcknowledgementCICDocument;

import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_ACKNOWLEDGEMENT_OF_SERVICE;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.*;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;


@Component
@Slf4j
public class CaseWorkerAcknowledgementOfService implements CCDConfig<CaseData, State, UserRole> {

    private static final String APPLICANT = "applicant";
    private static final String RESPONDENT = "respondent";
    private static final String REPRESENTATIVE = "representative";

    private static final CcdPageConfiguration partyStatementSelect = new PartyStatementSelect();
    private static final CcdPageConfiguration uploadPartyStatementDoc = new UploadPartyStatementDoc();

    private final StatementService statementService;

    @Autowired
    public CaseWorkerAcknowledgementOfService(StatementService statementService) {
        this.statementService = statementService;
    }

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Event.EventBuilder<CaseData, UserRole, State> eventBuilder =
            configBuilder
                .event(CASEWORKER_ACKNOWLEDGEMENT_OF_SERVICE)
                .forStates(Submitted)
                .name("Acknowledgment Of Service")
                .description("Add Acknowledgment Note")
                .grant(CREATE_READ_UPDATE,
                    ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_HEARING_CENTRE_ADMIN).publishToCamunda()
                .aboutToStartCallback(this::aboutToStart)
                .aboutToSubmitCallback(this::aboutToSubmit);

        PageBuilder pageBuilder = new PageBuilder(eventBuilder);
        partyStatementSelect.addTo(pageBuilder);
        uploadPartyStatementDoc.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        var caseData = details.getData();
        var cicCase = caseData.getCicCase();

        DynamicList parties = createDynamicList(List.of(APPLICANT, RESPONDENT, REPRESENTATIVE));

        cicCase.setWhichPartyStatementDynamicRadioList(parties);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();

    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {

        final CaseData caseData = details.getData();
        AcknowledgementCICDocument acknowledgmentDoc =
            caseData.getNewDocManagement().getAcknowledgementCICDocument();

        try {

            statementService.uploadPartyStatement(acknowledgmentDoc,
                caseData.getCicCase().getWhichPartyStatementDynamicRadioList().getValue().getLabel(),
                caseData.getHyphenatedCaseRef());

        } catch (Exception notificationException) {
            log.error("Exception When uploading to database", notificationException);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public static DynamicList createDynamicList(List<String> parties) {
        List<DynamicListElement> dynamicListElements = parties
            .stream()
            .map(party -> DynamicListElement.builder().label(party).code(UUID.randomUUID()).build())
            .toList();

        return DynamicList
            .builder()
            .listItems(dynamicListElements)
            .build();
    }

}
