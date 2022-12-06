package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;


public class ManageSelectOrderTemplates implements CcdPageConfiguration {


    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("dueDatesForOrders")
            .label("dueDatesForOrder", "<h1>Select an order\n</h1>")
            .complex(CaseData::getCicCase)
            .optional(CicCase::getDraftOrderCICList)
            .done();

    }


}
