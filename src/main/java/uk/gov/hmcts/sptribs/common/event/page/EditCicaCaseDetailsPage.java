package uk.gov.hmcts.sptribs.common.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.EditCicaCaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

@Slf4j
@Component
public class EditCicaCaseDetailsPage implements CcdPageConfiguration {


    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("editCaseDetails")
            .pageLabel("Case details")
            .complex(CaseData::getEditCicaCaseDetails)
                .optional(EditCicaCaseDetails::getCicaReferenceNumber)
                .optional(EditCicaCaseDetails::getCicaCaseWorker)
                .optional(EditCicaCaseDetails::getCicaCasePresentingOfficer)
                .done()
            .done();
//            .publishToCamunda(false);
    }


}
