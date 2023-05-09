package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class ManageFlagUpdate implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("caseworkerManageFlagUpdate")
            .pageLabel("Update flag")
            .label("LabelCaseworkerManageFlagUpdate", "")
            .complex(CaseData::getCicCase)
            .mandatoryWithLabel(CicCase::getFlagAdditionalDetail, "Explain why you are updating this flag.\n"
                + "Do not include any sensitive information such as personal details.")
            .mandatory(CicCase::getFlagStatus)
            .done();

    }

}
