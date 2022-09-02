package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class EditCaseCategorisationDetails  implements CcdPageConfiguration {
    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("editCaseCategorisationDetails")
            .pageLabel("Case Categorisation")
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getCaseCategory)
            .mandatory(CicCase::getCaseSubcategory)
            .done();
    }
}
