package uk.gov.hmcts.sptribs.caseworker.event.page;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.caseworker.model.ContactPartiesDocuments;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.services.cdam.CaseDocumentClientApi;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.sptribs.caseworker.util.ContactPartiesReviewUtil.setReviewDocuments;
import static uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil.requireSelectedContactPartiesDocuments;
import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.CONTACT_PARTIES_NOTIFICATION_FAILED;

@Component
@Slf4j
@RequiredArgsConstructor
public class ContactPartiesSelectDocument implements CcdPageConfiguration {

    private static final int MAX_DOCUMENT_COUNT = 10;
    private static final long TWO_MEGABYTES = 2_048_000;

    private final CaseDocumentClientApi caseDocumentClientApi;
    private final IdamService idamService;
    private final AuthTokenGenerator authTokenGenerator;

    @Value("${feature.citizen-dashboard.enabled}")
    private boolean citizenDashboardEnabled;

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("contactPartiesSelectDocument", this::midEvent)
            .pageLabel("Documents to include")
            .label("LabelContactPartiesSelectDocument", "Select a case document to include with this message(Optional)")
            .label("LabelContactPartiesSelectDocumentNote",
                "Note: Gov.Notify only supports sending documents in the formats of PDF, CSV, txt, rtf, MS Word Document "
                    + "file and MS Excel File. Your file must be smaller than 2MB")
            .complex(CaseData::getContactPartiesDocuments)
            .readonly(ContactPartiesDocuments::getActiveDocumentSlots, "[STATE]=\"ALWAYS_HIDE\"")
            .optionalNoSummary(ContactPartiesDocuments::getDocumentList, null, "Selected documents")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();

        DynamicMultiSelectList list = data.getContactPartiesDocuments().getDocumentList();
        if (list != null && list.getValue() != null && list.getValue().size() > MAX_DOCUMENT_COUNT) {
            errors.add("Select up to 10 documents");
        }

        if (errors.isEmpty() && (data.getCicCase() == null || !setReviewDocuments(data))) {
            errors.add(CONTACT_PARTIES_NOTIFICATION_FAILED);
        }

        if (errors.isEmpty() && !citizenDashboardEnabled && list != null && list.getValue() != null) {
            validateDocumentFileSizes(requireSelectedContactPartiesDocuments(data), errors);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }

    private void validateDocumentFileSizes(List<CaseworkerCICDocument> selectedDocuments, List<String> errors) {
        var systemUserAuth = idamService.retrieveSystemUpdateUserDetails().getAuthToken();
        var serviceAuth = authTokenGenerator.generate();

        for (CaseworkerCICDocument document : selectedDocuments) {
            String id = StringUtils.substringAfterLast(document.getDocumentLink().getUrl(), "/");
            var response = caseDocumentClientApi.getDocument(systemUserAuth, serviceAuth, UUID.fromString(id));
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to retrieve document with id " + id);
            }
            long size = response.getBody() != null ? response.getBody().size : TWO_MEGABYTES * 2;
            if (size > TWO_MEGABYTES) {
                errors.add("Unable to proceed because " + document.getDocumentLink().getFilename() + " is larger than 2MB");
            }
        }
    }

}
