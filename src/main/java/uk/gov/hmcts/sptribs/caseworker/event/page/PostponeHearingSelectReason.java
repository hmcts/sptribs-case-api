package uk.gov.hmcts.sptribs.caseworker.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

@Slf4j
@Component
public class PostponeHearingSelectReason implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("caseworkerPostponeHearingSelectReason")
            .pageLabel("Reasons for postponement")
            .label("LabelCaseworkerPostponeHearingSelectReason", "")
            .complex(CaseData::getListing)
            .mandatoryWithLabel(Listing::getPostponeReason, "Why was the hearing postponed?")
            .optional(Listing::getPostponeAdditionalInformation)
            .done();
    }

}
