package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.DocumentManagement;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.List;

import static uk.gov.hmcts.sptribs.document.DocumentUtil.validateCaseworkerCICDocumentFormat;

public class UploadCaseDocuments implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "newCaseworkerCICDocument=\"NEVER_SHOW\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("uploadCaseDocuments", this::midEvent)
            .pageLabel("Upload case documents")
            .label("LabelUploadCaseDocuments", "")
            .label("uploadCaseDocumentLabel", "Upload a copy of the order that you want to issue as part of this case")
            .label("uploadCaseDocumentConditions", """
                The order should be:
                 *  uploaded separately, not in one large file
                 *  a maximum of 100MB in size (larger files must be split)
                 *  labelled clearly, e.g. applicant-name-decision-notice.pdf
                 """
            )
            .complex(CaseData::getNewDocManagement)
            .mandatory(DocumentManagement::getCaseworkerCICDocument)
            .done()
            .complex(CaseData::getAllDocManagement)
            .readonly(DocumentManagement::getCaseworkerCICDocument, ALWAYS_HIDE)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();

        List<ListValue<CaseworkerCICDocument>> uploadedDocuments = data.getNewDocManagement().getCaseworkerCICDocument();
        List<String> errors = validateCaseworkerCICDocumentFormat(uploadedDocuments);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
}
