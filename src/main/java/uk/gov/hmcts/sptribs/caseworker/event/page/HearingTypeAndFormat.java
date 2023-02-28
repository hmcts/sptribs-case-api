package uk.gov.hmcts.sptribs.caseworker.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.RecordListing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

@Slf4j
@Component
public class HearingTypeAndFormat implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "cicCaseHearingList=\"NEVER_SHOW\"";


    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("hearingTypeAndFormat")
            .pageLabel("Hearing type and format")
            .label("labelHearingTypeAndFormat", "")
            .complex(CaseData::getRecordListing)
            .mandatory(RecordListing::getHearingType)
            .mandatory(RecordListing::getHearingFormat)
           .done();
    }


}
