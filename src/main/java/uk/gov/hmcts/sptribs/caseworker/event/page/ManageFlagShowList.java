package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;


public class ManageFlagShowList implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("caseworkerManageFlagSelect")
            .pageLabel("Select case flag")
            .label("LabelCaseworkerManageFlagSelect", "")
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getFlagDynamicList)
            .label("error", "<h2>There are no flags on case to manage</h2>", "cicCaseFlagDynamicList =\"\"")
            .done();

    }
}
