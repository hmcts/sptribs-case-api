package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.util.EventUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.List;

public class SendOrderNotifyParties implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "cicCaseCaseWorkerSendOrderMessage=\"NEVER_SHOW\"";
    private static final String RECIPIENT_LABEL = "Order information recipient";

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("caseWorkerSendOrderNotifyParties", this::midEvent)
            .pageLabel("Contact parties")
            .complex(CaseData::getCicCase)
            .label("caseWorkerSendOrderMessage", "Who should receive this Order?")
            .readonly(CicCase::getFullName, ALWAYS_HIDE)
            .optional(CicCase::getNotifyPartySubject, "cicCaseFullName!=\"\" ",
                "", RECIPIENT_LABEL, "${cicCaseFullName}")
            .readonly(CicCase::getRepresentativeFullName, ALWAYS_HIDE)
            .optional(CicCase::getNotifyPartyRepresentative, "cicCaseRepresentativeFullName!=\"\" ",
                "", RECIPIENT_LABEL, "${cicCaseRepresentativeFullName}")
            .readonly(CicCase::getRespondentName, ALWAYS_HIDE)
            .optional(CicCase::getNotifyPartyRespondent, "cicCaseRespondentName!=\"\" ",
                "", RECIPIENT_LABEL, "${cicCaseRespondentName}")
            .readonly(CicCase::getApplicantFullName, ALWAYS_HIDE)
            .optional(CicCase::getNotifyPartyApplicant, "cicCaseApplicantFullName!=\"\"",
                "", RECIPIENT_LABEL, "${cicCaseApplicantFullName}")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = EventUtil.checkRecipient(data);
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }

}

