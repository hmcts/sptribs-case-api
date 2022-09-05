package uk.gov.hmcts.sptribs.common.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class ApplicantDetails implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("editApplicantDetails")
            .pageLabel("Who is the applicant in this case?")
            .label("applicantDetailsObject", "")
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getApplicantFullName)
            .mandatory(CicCase::getApplicantPhoneNumber)
            .mandatory(CicCase::getApplicantEmailAddress)
            .optionalWithLabel(CicCase::getApplicantDateOfBirth, "")
            .mandatoryWithLabel(CicCase::getApplicantContactDetailsPreference, "")
            .mandatory(CicCase::getApplicantAddress, "cicCaseApplicantContactDetailsPreference = \"Post\"")
            .done();
    }
}
