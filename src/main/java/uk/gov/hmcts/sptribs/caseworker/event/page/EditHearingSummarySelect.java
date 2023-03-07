package uk.gov.hmcts.sptribs.caseworker.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

@Slf4j
@Component
public class EditHearingSummarySelect implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("editHearingSummarySelect")
            .pageLabel("Select hearing summary")
            .complex(CaseData::getCicCase)
            .mandatoryWithLabel(CicCase::getHearingSummaryList,"Choose a hearing summarise to edit")
            .done();
    }

}
