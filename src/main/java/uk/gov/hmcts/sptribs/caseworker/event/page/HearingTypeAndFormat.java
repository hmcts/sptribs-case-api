package uk.gov.hmcts.sptribs.caseworker.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.HearingSummary;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

@Slf4j
@Component
public class HearingTypeAndFormat implements CcdPageConfiguration {


    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("hearingSummaryHearingTypeAndFormat")
            .pageLabel("Hearing type and format")
            .label("LabelHearingSummaryHearingTypeAndFormat", "")
            .complex(CaseData::getHearingSummary)
            .mandatory(HearingSummary::getHearingType)
            .mandatory(HearingSummary::getHearingFormat)
            .done();
    }


}
