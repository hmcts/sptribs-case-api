package uk.gov.hmcts.sptribs.common.event.page;

import org.apache.commons.lang3.StringUtils;
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

public class RepresentativeDetails implements CcdPageConfiguration {
    @Override
    public void addTo(PageBuilder pageBuilder) {
        Map<String, String> map = new HashMap<>();
        map.put("applicantDetailsObjects", "cicCasePartiesCICCONTAINS \"ApplicantCIC\"");
        map.put("representativeDetailsObjects", "cicCasePartiesCICCONTAINS \"RepresentativeCIC\"");
        pageBuilder.page("representativeDetailsObjects", this::midEvent)
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

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();

        if (null != data.getCicCase()
            && ContactPreferenceType.POST == data.getCicCase().getRepresentativeContactDetailsPreference()
            && null != data.getCicCase().getRepresentativeAddress()) {
            if (StringUtils.isEmpty(data.getCicCase().getRepresentativeAddress().getCountry())) {
                errors.add("Country is mandatory");
            }
            if (StringUtils.isEmpty(data.getCicCase().getRepresentativeAddress().getPostCode())) {
                errors.add("PostCode is mandatory");
            }
            if (StringUtils.isEmpty(data.getCicCase().getRepresentativeAddress().getPostTown())) {
                errors.add("Town or City is mandatory");
            }
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
}
