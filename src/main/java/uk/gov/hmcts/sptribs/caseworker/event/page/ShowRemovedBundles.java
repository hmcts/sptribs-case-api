package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class ShowRemovedBundles implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("showRemovedBundles")
            .pageLabel("Removed bundles")
            .label("LabelShowRemovedBundles", "")
            .label("LabelShowRemovedBundlesWarning", "Below bundles will be removed")
            .readonly(CaseData::getRemovedCaseBundlesList)
            .done();
    }
}
