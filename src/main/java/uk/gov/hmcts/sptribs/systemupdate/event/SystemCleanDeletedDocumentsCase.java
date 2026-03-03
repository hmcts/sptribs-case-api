package uk.gov.hmcts.sptribs.systemupdate.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEM_UPDATE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class SystemCleanDeletedDocumentsCase implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_CLEAN_DELETED_DOCUMENTS = "system-clean-deleted-documents";

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Event.EventBuilder<CaseData, UserRole, State> eventBuilder = configBuilder
            .event(SYSTEM_CLEAN_DELETED_DOCUMENTS)
            .forAllStates()
            .name("Clean deleted documents")
            .description("Clean deleted documents that are stuck in further documents")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE_DELETE, SYSTEM_UPDATE);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> caseDetails,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        log.info("Clean deleted documents event about to clear stuck documents for caseId = {}",
            caseDetails.getId());

        final CaseData caseData = caseDetails.getData();

        List<ListValue<CaseworkerCICDocument>> allUploadedDocs =
            caseData.getAllDocManagement().getCaseworkerCICDocument();

        List<ListValue<CaseworkerCICDocument>> furtherDocs =
            caseData.getFurtherUploadedDocuments();

        if (furtherDocs == null || furtherDocs.isEmpty()) {
            return returnEarly(caseDetails, caseData);
        }

        List<ListValue<CaseworkerCICDocument>> caseworkerCICDocumentsToRemove =
            getCaseworkerCICDocumentsToRemove(allUploadedDocs, furtherDocs);

        if (caseworkerCICDocumentsToRemove.isEmpty()) {
            return returnEarly(caseDetails, caseData);
        }

        String deletedDocsString = caseworkerCICDocumentsToRemove.stream()
            .map(document -> document.getValue().getDocumentLink().getFilename())
            .collect(Collectors.joining(", "));

        furtherDocs.removeAll(caseworkerCICDocumentsToRemove);

        log.info("Clean deleted documents event cleared the following documents {} for caseId = {}",
            deletedDocsString, caseDetails.getId());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    private List<ListValue<CaseworkerCICDocument>> getCaseworkerCICDocumentsToRemove(
        List<ListValue<CaseworkerCICDocument>> allUploadedDocs,
        List<ListValue<CaseworkerCICDocument>> furtherDocs) {
        return furtherDocs.stream()
            .filter(furtherDoc -> {
                String furtherUrl = furtherDoc.getValue().getDocumentLink().getUrl();

                return allUploadedDocs.stream()
                    .map(doc -> doc.getValue().getDocumentLink().getUrl())
                    .noneMatch(furtherUrl::equals);
            })
            .toList();
    }

    private AboutToStartOrSubmitResponse<CaseData, State> returnEarly(CaseDetails<CaseData, State> caseDetails, CaseData caseData) {
        log.info("Clean deleted documents event cleaned no documents"
            + " for caseId = {}", caseDetails.getId());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
