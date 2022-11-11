package uk.gov.hmcts.sptribs.caseworker.event.page;


import uk.gov.hmcts.sptribs.caseworker.model.SendOrder;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.HashMap;
import java.util.Map;

public class SendOrderUploadOrder implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        String pageName = "uploadOrder";
        Map<String, String> map = new HashMap<>();
        map.put(pageName, "sendOrderOrderIssuingType = \"Upload a new order from your computer\"");
        pageBuilder.page(pageName)
            .label(pageName, "<h1>Upload an order\n</h1>")
            .pageShowConditions(map)
            .label("uploadMessage", "Upload a copy of the order that you want to issue as part of this case")
            .label("uploadLimits", "The order should be: \n"
                + "  *  a maximum of 100MB in size (larger files must be split \n"
                + "  *  labelled clearly, e.g. applicant-name-decision-notice.pdf,")
            .complex(CaseData::getSendOrder, "", "", "")
            .mandatoryWithLabel(SendOrder::getOrderFile, "")
            .done();
    }
}
