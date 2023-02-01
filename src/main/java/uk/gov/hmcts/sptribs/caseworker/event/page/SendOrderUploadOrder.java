package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.HashMap;
import java.util.Map;

public class SendOrderUploadOrder implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        String pageNameUploadOrder = "caseworkerSendOrderUploadOrder";
        String pageNameDraftOrder = "caseworkerSendOrderSelectDraftOrder";
        Map<String, String> map = new HashMap<>();
        map.put(pageNameDraftOrder, "cicCaseOrderIssuingType = \"DraftOrder\"");
        map.put(pageNameUploadOrder, "cicCaseOrderIssuingType = \"UploadOrder\"");
        pageBuilder.page(pageNameUploadOrder)
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
}
