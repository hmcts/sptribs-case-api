package uk.gov.hmcts.sptribs.common.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class CaseCategorisationDetails implements CcdPageConfiguration {
    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("caseCategorisationDetails")
            .pageLabel("Case categorisation")
            .complex(CaseData::getCicCase)
            .mandatoryWithLabel(CicCase::getCaseCategory, "Case category")
            .mandatoryWithLabel(CicCase::getCaseSubcategory, "Case sub-category")
            .done();
    }
}
