package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.HashMap;
import java.util.Map;


public class SendOrderAddDraftOrder implements CcdPageConfiguration {

    private final Map<String, String> pageShowCondition;

    public SendOrderAddDraftOrder(Map<String, String> pageShowCondition) {
        this.pageShowCondition = pageShowCondition;
    }

    public SendOrderAddDraftOrder() {
        this(new HashMap<>());
    }

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("caseworkerSendOrderSelectDraftOrder")
            .pageLabel("Select order")
            .label("LabelSelectOrder", "")
            .pageShowConditions(pageShowCondition)
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getDraftOrderDynamicList)
            .done();
    }


}
