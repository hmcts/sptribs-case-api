package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class RecordNotifyParties implements CcdPageConfiguration {

    private static final String NEVER_SHOW = "recordHearingType=\"NEVER_SHOW\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("recordListingNotifyPage")
            .pageLabel("Notify parties")
            .label("labelNotifyParties", "")
            .complex(CaseData::getCicCase)
            .label("recordListingNotifyPageMessage", "Which parties should be notified about this listing?")
            .readonly(CicCase::getFullName, NEVER_SHOW)
            .optional(CicCase::getRecordNotifyPartySubject, "cicCaseFullName!=\"\" ")
            .label("recordListingNotifyPageRepresentative", "")
            .readonly(CicCase::getRepresentativeFullName, NEVER_SHOW)
            .optional(CicCase::getRecordNotifyPartyRepresentative, "cicCaseRepresentativeFullName!=\"\" ")
            .label("recordListingNotifyPageRespondent", "")
            .readonly(CicCase::getRespondantName, NEVER_SHOW)
            .optional(CicCase::getRecordNotifyPartyRespondent, "cicCaseRespondantName!=\"\" ")
            .done();
    }

}
