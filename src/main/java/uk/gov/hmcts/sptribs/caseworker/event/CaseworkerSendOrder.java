package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.SendOrderAddDraftOrder;
import uk.gov.hmcts.sptribs.caseworker.event.page.SendOrderNotifyParties;
import uk.gov.hmcts.sptribs.caseworker.event.page.SendOrderOrderDueDates;
import uk.gov.hmcts.sptribs.caseworker.event.page.SendOrderOrderIssuingSelect;
import uk.gov.hmcts.sptribs.caseworker.event.page.SendOrderUploadOrder;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingApplicant1Response;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingConditionalOrder;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseStayed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.NewCaseReceived;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Rejected;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Withdrawn;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class CaseworkerSendOrder implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_SEND_ORDER = "caseworker-send-order";
    private static final CcdPageConfiguration orderIssuingSelect = new SendOrderOrderIssuingSelect();
    private static final CcdPageConfiguration uploadOrder = new SendOrderUploadOrder();
    private static final CcdPageConfiguration draftOrder = new SendOrderAddDraftOrder();
    private static final CcdPageConfiguration orderDueDates = new SendOrderOrderDueDates();
    private static final CcdPageConfiguration notifyParties = new SendOrderNotifyParties();

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        var pageBuilder = send(configBuilder);
        orderIssuingSelect.addTo(pageBuilder);
        draftOrder.addTo(pageBuilder);
        uploadOrder.addTo(pageBuilder);
        orderDueDates.addTo(pageBuilder);
        notifyParties.addTo(pageBuilder);
    }

    public PageBuilder send(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(CASEWORKER_SEND_ORDER)
            .forStates(AwaitingApplicant1Response,
                AwaitingApplicant2Response,
                AwaitingConditionalOrder,
                Withdrawn,
                Rejected,
                NewCaseReceived,
                CaseManagement,
                AwaitingHearing,
                AwaitingOutcome,
                CaseClosed,
                CaseStayed)
            .name("Send order")
            .description("Send order")
            .showEventNotes()
            .showSummary()
            .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER)
            .grantHistoryOnly(SOLICITOR));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        var caseData = details.getData();
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public SubmittedCallbackResponse sent(CaseDetails<CaseData, State> details,
                                          CaseDetails<CaseData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# ")
            .build();
    }
}
