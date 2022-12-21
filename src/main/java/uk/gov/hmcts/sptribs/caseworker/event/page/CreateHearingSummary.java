package uk.gov.hmcts.sptribs.caseworker.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

@Slf4j
@Component
public class CreateHearingSummary implements CcdPageConfiguration {


    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("createHearingSummary")
            .pageLabel("Select hearing")
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getHearingSummaryList)
            .done();
    }


}
