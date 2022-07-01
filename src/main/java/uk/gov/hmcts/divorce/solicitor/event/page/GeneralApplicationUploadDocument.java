package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.ciccase.model.CaseData;
import uk.gov.hmcts.divorce.ciccase.model.GeneralApplication;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;

public class GeneralApplicationUploadDocument implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder.page("generalApplicationUploadDocument")
            .pageLabel("Upload document")
            .complex(CaseData::getGeneralApplication)
                .optional(GeneralApplication::getGeneralApplicationDocument)
                .optional(GeneralApplication::getGeneralApplicationDocumentComments)
            .done();
    }
}
