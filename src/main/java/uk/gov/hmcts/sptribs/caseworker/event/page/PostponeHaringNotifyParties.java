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

import static uk.gov.hmcts.sptribs.caseworker.util.CheckRequiredUtil.checkNullSubjectRepresentativeRespondent;

public class PostponeHaringNotifyParties implements CcdPageConfiguration {

    private static final String NEVER_SHOW = "cicCasePostponeReason=\"NEVER_SHOW\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("caseworkerPostponeHearingNotifyParties", this::midEvent)
            .pageLabel("Notify parties")
            .complex(CaseData::getCicCase)
            .label("caseworkerPostponeHearingNotifyPartiesMessage", "Which parties should be notified about the change to this listing?")
            .readonly(CicCase::getFullName, NEVER_SHOW)
            .optional(CicCase::getNotifyPartySubject, "cicCaseFullName!=\"\" ")
            .readonly(CicCase::getRepresentativeFullName, NEVER_SHOW)
            .optional(CicCase::getNotifyPartyRepresentative, "cicCaseRepresentativeFullName!=\"\" ")
            .readonly(CicCase::getRespondantName, NEVER_SHOW)
            .optional(CicCase::getNotifyPartyRespondent, "cicCaseRespondantName!=\"\" ")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();

        if (checkNullSubjectRepresentativeRespondent(data)) {
            errors.add("One field must be selected.");
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }


}

