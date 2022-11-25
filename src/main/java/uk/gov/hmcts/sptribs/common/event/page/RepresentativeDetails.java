package uk.gov.hmcts.sptribs.common.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.HashMap;
import java.util.Map;

public class RepresentativeDetails implements CcdPageConfiguration {
    @Override
    public void addTo(PageBuilder pageBuilder) {
        Map<String, String> map = new HashMap<>();
        map.put("applicantDetailsObjects","cicCasePartiesCICCONTAINS \"ApplicantCIC\"");
        map.put("representativeDetailsObjects","cicCasePartiesCICCONTAINS \"RepresentativeCIC\"");
        pageBuilder.page("representativeDetailsObjects")
            .pageLabel("Who is the Representative for this case?")
            .label("LabelRepresentative", "")
            .pageShowConditions(map)
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getRepresentativeFullName)
            .optional(CicCase::getRepresentativeOrgName)
            .mandatory(CicCase::getRepresentativePhoneNumber)
            .optional(CicCase::getRepresentativeReference)
            .mandatoryWithLabel(CicCase::getIsRepresentativeQualified, "")
            .mandatory(CicCase::getRepresentativeContactDetailsPreference)
            .mandatory(CicCase::getRepresentativeEmailAddress, "cicCaseRepresentativeContactDetailsPreference = \"Email\"")
            .mandatory(CicCase::getRepresentativeAddress, "cicCaseRepresentativeContactDetailsPreference = \"Post\"")
            .done();
    }
}
