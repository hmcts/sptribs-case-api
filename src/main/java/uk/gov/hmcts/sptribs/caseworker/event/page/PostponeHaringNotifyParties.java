package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.sptribs.caseworker.util.CheckRequiredUtil.checkNullRecordSubjectRepresentativeRespondent;

public class PostponeHaringNotifyParties implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "cicCasePostponeReason=\"ALWAYS_HIDE\"";

    @Override

    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("caseworkerPostponeHearingNotifyParties", this::midEvent)
            .pageLabel("Notify parties")
            .label("LabelCaseworkerPostponeHearingNotifyParties", "")
            .complex(CaseData::getCicCase)
            .label("caseworkerPostponeHearingNotifyPartiesMessage", "Which parties should be notified this Postponement?")
            .readonly(CicCase::getFullName, ALWAYS_HIDE)
            .optionalWithoutDefaultValue(CicCase::getNotifyPartySubject,
                "cicCaseFullName!=\"\" ",
                "Postpone information recipient - Subject")
            .readonly(CicCase::getRepresentativeFullName, ALWAYS_HIDE)
            .optionalWithoutDefaultValue(CicCase::getNotifyPartyRepresentative,
                "cicCaseRepresentativeFullName!=\"\" ",
                "Postpone information recipient - Representative")
            .readonly(CicCase::getRespondantName, ALWAYS_HIDE)
            .optionalWithoutDefaultValue(CicCase::getNotifyPartyRespondent,
                "cicCaseRespondantName!=\"\" ",
                "Postpone information recipient - Respondent")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();

        if (checkNullRecordSubjectRepresentativeRespondent(data)) {
            errors.add("At least one party must be selected.");
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }


}

