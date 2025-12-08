package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;


public class SendOrderAddDraftOrder implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("caseworkerSendOrderSelectDraftOrder")
            .pageLabel("Select order")
            .label("LabelSelectOrder", "")
            .pageShowConditions(PageShowConditionsUtil.createAndSendOrderConditions())
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getDraftOrderDynamicList)
            .done();
    }

}
