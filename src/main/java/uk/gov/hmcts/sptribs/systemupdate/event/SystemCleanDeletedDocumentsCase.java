package uk.gov.hmcts.sptribs.systemupdate.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEM_UPDATE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class SystemCleanDeletedDocumentsCase implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_CLEAN_DELETED_DOCUMENTS = "system-clean-deleted-documents";

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(SYSTEM_CLEAN_DELETED_DOCUMENTS)
            .forAllStates()
            .name("Clean deleted documents")
            .description("Clean deleted documents that are stuck in further documents")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE_DELETE, SYSTEM_UPDATE);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> caseDetails,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        Long caseId = caseDetails.getId();
        log.info("Clean deleted documents event about to clear stuck documents for caseId = {}", caseId);

        CaseData caseData = caseDetails.getData();
        List<ListValue<CaseworkerCICDocument>> furtherDocs = caseData.getFurtherUploadedDocuments();

        if (furtherDocs == null || furtherDocs.isEmpty()) {
            return returnEarly(caseDetails, caseData);
        }

        List<ListValue<CaseworkerCICDocument>> allDocs =
            caseData.getAllDocManagement().getCaseworkerCICDocument();

        List<ListValue<CaseworkerCICDocument>> docsToRemove =
            findDocumentsToRemove(allDocs, furtherDocs);

        if (docsToRemove.isEmpty()) {
            return returnEarly(caseDetails, caseData);
        }

        logDeletedDocuments(caseId, docsToRemove);

        furtherDocs.removeAll(docsToRemove);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    private List<ListValue<CaseworkerCICDocument>> findDocumentsToRemove(
        List<ListValue<CaseworkerCICDocument>> allDocs,
        List<ListValue<CaseworkerCICDocument>> furtherDocs) {

        Set<String> uploadedUrls = allDocs.stream()
            .map(this::getUrl)
            .collect(Collectors.toSet());

        return furtherDocs.stream()
            .filter(doc -> !uploadedUrls.contains(getUrl(doc)))
            .toList();
    }

    private void logDeletedDocuments(Long caseId,
                                     List<ListValue<CaseworkerCICDocument>> docs) {

        String filenames = docs.stream()
            .map(this::getFilename)
            .collect(Collectors.joining(", "));

        log.info(
            "Clean deleted documents event cleared the following documents {} for caseId = {}",
            filenames,
            caseId
        );
    }

    private AboutToStartOrSubmitResponse<CaseData, State> returnEarly(CaseDetails<CaseData, State> caseDetails, CaseData caseData) {
        log.info("Clean deleted documents event found no further documents to clean for caseId = {}", caseDetails.getId());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    private String getUrl(ListValue<CaseworkerCICDocument> documentListValue) {
        return documentListValue.getValue().getDocumentLink().getUrl();
    }

    private String getFilename(ListValue<CaseworkerCICDocument> documentListValue) {
        return documentListValue.getValue().getDocumentLink().getFilename();
    }
}
