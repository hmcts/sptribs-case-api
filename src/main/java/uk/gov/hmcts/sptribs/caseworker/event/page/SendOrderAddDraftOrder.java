package uk.gov.hmcts.sptribs.caseworker.event.page;


import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.HashMap;
import java.util.Map;

public class SendOrderAddDraftOrder implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        String pageNameUploadOrder = "uploadOrder";
        String pageNameDraftOrder = "selectDraftOrder";
        Map<String, String> map = new HashMap<>();
        map.put(pageNameDraftOrder, "sendOrderOrderIssuingType = \"DraftOrder\"");
        map.put(pageNameUploadOrder, "sendOrderOrderIssuingType = \"UploadOrder\"");
        pageBuilder.page(pageNameDraftOrder)
            .label(pageNameDraftOrder, "<h1>Select order\n</h1>")
            .pageShowConditions(map)
            .complex(CaseData::getDraftOrderCIC, "", "", "")
            .readonly(DraftOrderCIC::getOrderTemplate)
            .mandatory(DraftOrderCIC::getMainContentForGeneralDirections)
            .done();
    }
}
