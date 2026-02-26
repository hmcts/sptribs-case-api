package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class SelectBundles implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("selectBundles")
            .pageLabel("Select bundles to delete")
            .label("LabelSelectBundle", "")
            .label("LabelSelectBundleWarning", "")
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getRemoveBundlesList)
            .done();
    }
}
