package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.model.CICDocument;

import java.util.List;

import static uk.gov.hmcts.sptribs.document.DocumentUtil.validateDocumentFormat;

public class SendOrderUploadOrder implements CcdPageConfiguration {


    @Override
    public void addTo(PageBuilder pageBuilder) {
        String pageNameUploadOrder = "caseworkerSendOrderUploadOrder";
        pageBuilder.page(pageNameUploadOrder, this::midEvent)
            .pageLabel("Upload an order")
            .label("LabelPageNameUploadOrder","")
            .pageShowConditions(PageShowConditionsUtil.createAndSendOrderConditions())
            .label("uploadMessage", "Upload a copy of the document you wish to be added to case file view")
            .label("uploadLimits", """
                The order should be:
                 *  uploaded separately, not one large file
                 *  a maximum of 100MB in size (larger files must be split)
                 *  labelled clearly, e.g. applicant-name-decision-notice.pdf




                 Note: If the remove button is disabled, please refresh the page to remove attachments
                 """
            )
            .complex(CaseData::getCicCase)
                .mandatory(CicCase::getOrderFile)
                .done()
            .readonly(CaseData::getCurrentEvent, "LabelPageNameUploadOrder=\"HIDDEN\"");

    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                   CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        List<ListValue<CICDocument>> uploadedDocuments = data.getCicCase().getOrderFile();
        final List<String> errors = validateDocumentFormat(uploadedDocuments);

        data.getCicCase().setOrderTemplateIssued(data.getCicCase().getOrderFile().getFirst().getValue().getDocumentLink());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
}
