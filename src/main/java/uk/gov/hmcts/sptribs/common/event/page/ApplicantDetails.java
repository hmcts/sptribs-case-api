package uk.gov.hmcts.sptribs.common.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.HashMap;
import java.util.Map;

public class ApplicantDetails implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        Map<String, String> map = new HashMap<>();
        map.put("applicantDetailsObjects","cicCasePartiesCICCONTAINS \"ApplicantCIC\"");
        map.put("representativeDetailsObjects","cicCasePartiesCICCONTAINS \"RepresentativeCIC\"");
        pageBuilder.page("applicantDetailsObjects")
            .pageLabel("Who is the applicant in this case?")
            .label("applicantDetails", "")
            .pageShowConditions(map)
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
