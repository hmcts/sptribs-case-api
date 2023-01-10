package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class RecordNotifyParties implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("recordListingNotifyPage")
            .label("recordListingNotifyPageLabel", "<h1>Notify page</h1>")
            .complex(CaseData::getCicCase)
            .label("recordListingNotifyPageMessage", "Which parties would be notified of this listing?")
            .readonlyWithLabel(CicCase::getFullName, " ")
            .optional(CicCase::getRecordNotifyPartySubject, "cicCaseFullName!=\"\" ")
            .label("recordListingNotifyPageRepresentative", "")
            .readonlyWithLabel(CicCase::getRepresentativeFullName, " ")
            .optional(CicCase::getRecordNotifyPartyRepresentative, "cicCaseRepresentativeFullName!=\"\" ")
            .label("recordListingNotifyPageRespondent", "")
            .readonlyWithLabel(CicCase::getRespondantName, " ")
            .optional(CicCase::getRecordNotifyPartyRespondent, "cicCaseRespondantName!=\"\" ")
            .done();
    }

}
