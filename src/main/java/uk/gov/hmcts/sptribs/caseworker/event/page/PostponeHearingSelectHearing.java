package uk.gov.hmcts.sptribs.caseworker.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

@Slf4j
@Component
public class PostponeHearingSelectHearing implements CcdPageConfiguration {


    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("caseworkerPostponeHearingSelectHearing")
            .label("caseworkerPostponeHearingSelectHearing","<h1>Select hearing</h1>")
            .complex(CaseData::getCicCase)
            .mandatoryWithLabel(CicCase::getHearingList,"Choose a hearing to postpone")
            .done();
    }


}
