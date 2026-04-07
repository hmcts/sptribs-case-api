package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class ShowRemovedDraftOrders implements CcdPageConfiguration {


    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("showRemovedDraftOrders")
            .pageLabel("Removed draft orders")
            .label("LabelShowRemovedDraftOrders", "")
            .label("LabelShowRemovedDraftOrdersWarning", "Below draft orders will be removed")
            .complex(CaseData::getCicCase)
            .readonly(CicCase::getRemovedDraftList)
            .done();
    }


}
