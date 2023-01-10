package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.HashMap;
import java.util.Map;


public class SendOrderAddDraftOrder implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        String pageNameUploadOrder = "caseworkerSendOrderUploadOrder";
        String pageNameDraftOrder = "caseworkerSendOrderSelectDraftOrder";
        Map<String, String> map = new HashMap<>();
        map.put(pageNameDraftOrder, "cicCaseOrderIssuingType = \"DraftOrder\"");
        map.put(pageNameUploadOrder, "cicCaseOrderIssuingType = \"UploadOrder\"");
        pageBuilder.page(pageNameDraftOrder)
            .pageLabel("Select order")
            .pageShowConditions(map)
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getDraftList)
            .readonlyNoSummary(CicCase::getDraftOrderCICList)
            .label("errorMessageNoDraftOrder", "<h2>There are no draft orders on this case</h2>", "cicCaseDraftOrderCICList =\"\"")
            .done();
    }


}
