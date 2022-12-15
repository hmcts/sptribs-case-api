package uk.gov.hmcts.sptribs.common.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.ContactParties;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

@Slf4j
@Component
public class PartiesToContact implements CcdPageConfiguration {


    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("partiesToContact")
            .pageLabel("Contact Parties")
            .complex(CaseData::getCicCase)
            .optional(CicCase::getContactPartiesCIC)
            .done()
            .complex(CaseData::getContactParties)
            .mandatory(ContactParties::getMessage)
            .done();
    }


}

