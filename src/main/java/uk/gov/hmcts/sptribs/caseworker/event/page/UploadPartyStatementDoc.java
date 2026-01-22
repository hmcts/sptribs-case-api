package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.DocumentManagement;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.model.AcknowledgementCICDocument;

import java.util.List;

import static uk.gov.hmcts.sptribs.document.DocumentUtil.validateAcknowledgementCICDocumentFormat;

public class UploadPartyStatementDoc implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("uploadAcknowledgementDocuments", this::midEvent)
            .pageLabel("Upload Acknowledgement documents")
            .label("LabelUploadAcknowledgementDocuments", "")
            .label("uploadAcknowledgementDocumentLabel", "Upload a copy of the document you wish to be added to case file view")
            .label("uploadAcknowledgementDocumentConditions", """
                The order should be:
                 *  uploaded separately, not one large file
                 *  a maximum of 100MB in size (larger files must be split)
                 *  labelled clearly, e.g. applicant-name-decision-notice.pdf




                 Note: If the remove button is disabled, please refresh the page to remove attachments
                 """
            )
            .complex(CaseData::getNewDocManagement)
            .mandatory(DocumentManagement::getAcknowledgementCICDocument)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();

        AcknowledgementCICDocument uploadedDocument = data.getNewDocManagement().getAcknowledgementCICDocument();
        List<String> errors = validateAcknowledgementCICDocumentFormat(uploadedDocument);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
}
