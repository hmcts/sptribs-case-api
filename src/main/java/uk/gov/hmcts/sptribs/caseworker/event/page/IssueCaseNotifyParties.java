package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class IssueCaseNotifyParties implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("issueCaseNotifyParties")
            .label("issueCaseNotifyParties", "<h1>Notify other parties</h1>")
            .complex(CaseData::getCicCase)
            .label("message", "Which other parties should be notified that the case has been issued to respondent?")
            .readonlyWithLabel(CicCase::getFullName, " ")
            .optional(CicCase::getNotifyPartySubject, "cicCaseFullName!=\"\" ")
            .label("app", "")
            .readonlyWithLabel(CicCase::getRepresentativeFullName, " ")
            .optional(CicCase::getNotifyPartyRepresentative, "cicCaseRepresentativeFullName!=\"\" ")
            .label("rep", "")
            .readonlyWithLabel(CicCase::getApplicantFullName, " ")
            .optional(CicCase::getNotifyPartyApplicant, "cicCaseApplicantFullName!=\"\" ")
            .done();
    }

}

