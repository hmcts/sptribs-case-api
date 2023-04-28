package uk.gov.hmcts.sptribs.caseworker.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

@Slf4j
@Component
public class HearingOutcomePage implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("hearingOutcome")
            .pageLabel("Hearing outcome")
            .pageShowConditions(PageShowConditionsUtil.editSummaryShowConditions())
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getHearingOutcome)
            .mandatory(CicCase::getAdjournmentReasons,"cicCaseHearingOutcome= \"Adjourned\"")
            .optional(CicCase::getOtherDetailsOfAdjournment,
                "cicCaseAdjournmentReasons= \"Other\" AND cicCaseHearingOutcome= \"Adjourned\" ")
            .done();
    }
}
