package uk.gov.hmcts.sptribs.common.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class CreateDraftOrder implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("createDraftOrder")
            .pageLabel("Create order")
            .label("LabelCreateDraftOrder", "")
            .label("createDraftOrder", "Draft to be created")
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getAnOrderTemplates)

            .done();
    }


}
