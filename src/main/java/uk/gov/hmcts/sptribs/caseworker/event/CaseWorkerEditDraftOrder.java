package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.event.page.EditDraftOrder;
import uk.gov.hmcts.sptribs.common.event.page.PreviewDraftOrder;

import static uk.gov.hmcts.sptribs.ciccase.model.State.POST_SUBMISSION_STATES_WITH_WITHDRAWN_AND_REJECTED;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;


@Component
@Slf4j
public class CaseWorkerEditDraftOrder implements CCDConfig<CaseData, State, UserRole> {
    private static final CcdPageConfiguration editDraftOrder = new EditDraftOrder();
    private static final CcdPageConfiguration previewDraftOrder = new PreviewDraftOrder();

    public static final String CASEWORKER_EDIT_DRAFT_ORDER = "caseworker-edit-draft-order";


    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(
            configBuilder
                .event(CASEWORKER_EDIT_DRAFT_ORDER)
                .forStates(POST_SUBMISSION_STATES_WITH_WITHDRAWN_AND_REJECTED)
                .name("Edit draft order")
                .showSummary()
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::draftCreated)
                .showEventNotes()
                .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER)
                .grantHistoryOnly(SOLICITOR));

        editDraftOrder.addTo(pageBuilder);
        previewDraftOrder.addTo(pageBuilder);


    }




    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {


        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .build();

    }

    public SubmittedCallbackResponse draftCreated(CaseDetails<CaseData, State> details,
                                                  CaseDetails<CaseData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("When you have finished drafting this order,you can send it to parties in this case.")
            .build();
    }
}
