package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;

public class ReinstateNotifyParties implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("notifyPartiesReinstate", this::midEvent)
            .label("notifyPartiesReinstate", "<h1>Contact parties</h1>")
            .complex(CaseData::getCicCase)
            .label("messageReinstate", "Who should be notified about this reinstatement?")
            .readonlyWithLabel(CicCase::getFullName, " ")
            .optional(CicCase::getNotifyPartySubject, "cicCaseFullName!=\"\" ")
            .label("emptyLabelBeforeRepresentativeReinstate", "")
            .readonlyWithLabel(CicCase::getRepresentativeFullName, " ")
            .optional(CicCase::getNotifyPartyRepresentative, "cicCaseRepresentativeFullName!=\"\" ")
            .label("emptyLabelBeforeRespondentReinstate", "")
            .readonlyWithLabel(CicCase::getRespondantName, " ")
            .optional(CicCase::getNotifyPartyRespondent, "cicCaseRespondantName!=\"\" ")
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
            && CollectionUtils.isEmpty(data.getCicCase().getNotifyPartySubject())
            && CollectionUtils.isEmpty(data.getCicCase().getNotifyPartyRepresentative())
            && CollectionUtils.isEmpty(data.getCicCase().getNotifyPartyRespondent());

    }
}

