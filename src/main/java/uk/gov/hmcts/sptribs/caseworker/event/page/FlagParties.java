package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.sptribs.caseworker.util.CheckRequiredUtil.checkMultiSubjectRepresentativeApplicant;
import static uk.gov.hmcts.sptribs.caseworker.util.CheckRequiredUtil.checkNullFlagSubjectRepresentativeApplicant;

public class FlagParties implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        Map<String, String> map = new HashMap<>();
        map.put("selectFlagParties", "caseFlagFlagLevel = \"PartyLevel\"");

        pageBuilder.page("selectFlagParties", this::midEvent)
            .label("selectFlagParties", "<h2>Where should this flag be added?\n Party Flag applied to:</h2>")
            .pageShowConditions(map)
            .complex(CaseData::getCicCase)
            .readonlyWithLabel(CicCase::getFullName, " ")
            .optional(CicCase::getFlagPartySubject, "cicCaseFullName!=\"\" ")
            .label("emptyLabelBeforeApplicantFlag", "")
            .readonlyWithLabel(CicCase::getApplicantFullName, " ")
            .optional(CicCase::getFlagPartyApplicant, "cicCaseApplicantFullName!=\"\" ")
            .label("emptyLabelBeforeRepresentativeFlag", "")
            .readonlyWithLabel(CicCase::getRepresentativeFullName, " ")
            .optional(CicCase::getFlagPartyRepresentative, "cicCaseRepresentativeFullName!=\"\" ")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();

        if (checkNullFlagSubjectRepresentativeApplicant(data)) {
            errors.add("One field must be selected.");
        } else if (checkMultiSubjectRepresentativeApplicant(data)) {
            errors.add("Only one field must be selected.");
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }


}

