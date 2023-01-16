package uk.gov.hmcts.sptribs.caseworker.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.RecordListing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

@Slf4j
@Component
public class ListingChangeReason implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("recordListingChangeReason")
            .pageLabel("Reason for listing change")
            .complex(CaseData::getRecordListing)
            .mandatory(RecordListing::getRecordListingChangeReason)
            .done();
    }
}
