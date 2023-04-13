package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.model.CICDocument;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.sptribs.document.DocumentUtil.validateDocumentFormat;

public class SendOrderUploadOrder implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        String pageNameUploadOrder = "caseworkerSendOrderUploadOrder";
        String pageNameDraftOrder = "caseworkerSendOrderSelectDraftOrder";
        Map<String, String> map = new HashMap<>();
        map.put(pageNameDraftOrder, "cicCaseOrderIssuingType = \"DraftOrder\"");
        map.put(pageNameUploadOrder, "cicCaseOrderIssuingType = \"UploadOrder\"");
        pageBuilder.page(pageNameUploadOrder, this::midEvent)
            .pageLabel("Upload an order")
            .label("LabelPageNameUploadOrder","")
            .pageShowConditions(map)
            .label("uploadMessage", "Upload a copy of the order that you want to issue as part of this case")
            .label("uploadLimits", """
                The order should be:
                 *  a maximum of 100MB in size (larger files must be split)
                 *  labelled clearly, e.g. applicant-name-decision-notice.pdf"""
            )
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getOrderFile)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                   CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        List<ListValue<CICDocument>> uploadedDocuments = data.getCicCase().getOrderFile();
        final List<String> errors = validateDocumentFormat(uploadedDocuments);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
}
