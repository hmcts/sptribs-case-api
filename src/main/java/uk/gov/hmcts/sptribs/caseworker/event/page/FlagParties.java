package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CaseSubcategory;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.sptribs.caseworker.util.CheckRequiredUtil.checkMultiSubjectRepresentativeApplicant;
import static uk.gov.hmcts.sptribs.caseworker.util.CheckRequiredUtil.checkNullSubjectRepresentativeApplicant;
import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.MINOR_FATAL_SUBJECT_ERROR_MESSAGE;
import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.SELECT_AT_LEAST_ONE_ERROR_MESSAGE;

public class FlagParties implements CcdPageConfiguration {
    private static final String RECIPIENT_LABEL = "Case Flag  information recipient";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        Map<String, String> map = new HashMap<>();
        map.put("caseworkerCaseFlagSelectFlagParties", "cicCaseFlagLevel = \"PartyLevel\"");

        pageBuilder.page("caseworkerCaseFlagSelectFlagParties", this::midEvent)
            .pageLabel("Where should this flag be added?")
            .label("LabelCaseworkerCaseFlagSelectFlagParties", "")
            .pageShowConditions(map)
            .complex(CaseData::getCicCase)
            .readonlyWithLabel(CicCase::getFullName, " ")
            .optionalWithoutDefaultValue(CicCase::getNotifyPartySubject,
                "cicCaseFullName!=\"\" ", RECIPIENT_LABEL)

            .label("LabelCaseworkerCaseFlagSelectFlagPartiesSubject", "")
            .readonlyWithLabel(CicCase::getRepresentativeFullName, " ")
            .optionalWithoutDefaultValue(CicCase::getNotifyPartyRepresentative,
                "cicCaseRepresentativeFullName!=\"\" ", RECIPIENT_LABEL)

            .label("LabelCaseworkerCaseFlagSelectFlagPartiesApplicant", "")
            .readonlyWithLabel(CicCase::getApplicantFullName, " ")
            .optionalWithoutDefaultValue(CicCase::getNotifyPartyApplicant,
                "cicCaseApplicantFullName!=\"\" ", RECIPIENT_LABEL)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();

        if (checkNullSubjectRepresentativeApplicant(data)) {
            errors.add(SELECT_AT_LEAST_ONE_ERROR_MESSAGE);
        } else if (checkMultiSubjectRepresentativeApplicant(data)) {
            errors.add("Only one field must be selected.");
        } else if ((data.getCicCase().getCaseSubcategory() == CaseSubcategory.FATAL
            || data.getCicCase().getCaseSubcategory() == CaseSubcategory.MINOR)
            && !CollectionUtils.isEmpty(data.getCicCase().getNotifyPartySubject())) {
            errors.add(MINOR_FATAL_SUBJECT_ERROR_MESSAGE);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }


}
