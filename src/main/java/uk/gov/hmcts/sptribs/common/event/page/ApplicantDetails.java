package uk.gov.hmcts.sptribs.common.event.page;

import org.apache.groovy.parser.antlr4.util.StringUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicantDetails implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        Map<String, String> map = new HashMap<>();
        map.put("applicantDetailsObjects", "cicCasePartiesCICCONTAINS \"ApplicantCIC\"");
        map.put("representativeDetailsObjects", "cicCasePartiesCICCONTAINS \"RepresentativeCIC\"");
        pageBuilder.page("applicantDetailsObjects", this::midEvent)
            .pageLabel("Who is the applicant in this case?")
            .label("applicantDetailsLabel", "")
            .pageShowConditions(map)
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getApplicantFullName)
            .mandatory(CicCase::getApplicantPhoneNumber)
            .optionalWithLabel(CicCase::getApplicantDateOfBirth, "")
            .mandatoryWithLabel(CicCase::getApplicantContactDetailsPreference, "")
            .mandatory(CicCase::getApplicantAddress, "cicCaseApplicantContactDetailsPreference = \"Post\"")
            .mandatory(CicCase::getApplicantEmailAddress, "cicCaseApplicantContactDetailsPreference = \"Email\"")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();

        if (null != data.getCicCase()
            && ContactPreferenceType.POST == data.getCicCase().getApplicantContactDetailsPreference()
            && null != data.getCicCase().getApplicantAddress()) {
            if (StringUtils.isEmpty(data.getCicCase().getApplicantAddress().getCountry())) {
                errors.add("Country is mandatory");
            }
            if (StringUtils.isEmpty(data.getCicCase().getApplicantAddress().getPostCode())) {
                errors.add("PostCode is mandatory");
            }
            if (StringUtils.isEmpty(data.getCicCase().getApplicantAddress().getPostTown())) {
                errors.add("Town or City is mandatory");
            }
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
}
