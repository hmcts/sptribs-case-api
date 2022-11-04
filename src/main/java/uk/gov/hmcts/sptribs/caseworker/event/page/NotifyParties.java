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

public class NotifyParties implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("notifyParties", this::midEvent)
            .label("notifyParties", "<h1>Contact parties</h1>")
            .complex(CaseData::getCicCase)
            .label("message", "Who should be notified about this reinstatement?")
            .readonlyWithLabel(CicCase::getFullName, " ")
            .optional(CicCase::getNotifyPartySubject, "cicCaseFullName!=\"\" ")
            .label("app", "")
            .readonlyWithLabel(CicCase::getApplicantFullName, " ")
            .optional(CicCase::getNotifyPartyApplicant, "cicCaseApplicantFullName!=\"\" ")
            .label("rep", "")
            .readonlyWithLabel(CicCase::getRepresentativeFullName, " ")
            .optional(CicCase::getNotifyPartyRepresentative, "cicCaseRepresentativeFullName!=\"\" ")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                   CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();

        if (checkNull(data)) {
            errors.add("One field must be selected.");
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }

    private boolean checkNull(CaseData data) {
        return null != data.getCicCase()
            && (null == data.getCicCase().getNotifyPartySubject() || data.getCicCase().getNotifyPartySubject().isEmpty())
            && (null == data.getCicCase().getNotifyPartyApplicant() || data.getCicCase().getNotifyPartyApplicant().isEmpty())
            && (null == data.getCicCase().getNotifyPartyRepresentative() || data.getCicCase().getNotifyPartyRepresentative().isEmpty());

    }
}

