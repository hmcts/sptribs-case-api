package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderContentCIC;
import uk.gov.hmcts.sptribs.caseworker.service.OrderService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.event.page.DraftOrderMainContentPage;
import uk.gov.hmcts.sptribs.common.event.page.EditDraftOrder;
import uk.gov.hmcts.sptribs.common.event.page.PreviewDraftOrder;

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_EDIT_DRAFT_ORDER;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseStayed;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;


@Component
@Slf4j
public class CaseWorkerEditDraftOrder implements CCDConfig<CaseData, State, UserRole> {

    private static final CcdPageConfiguration editDraftOrder = new EditDraftOrder();

    private static final CcdPageConfiguration previewOrder = new PreviewDraftOrder();

    private static final CcdPageConfiguration draftOrderEditMainContentPage = new DraftOrderMainContentPage();

    @Autowired
    private OrderService orderService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        PageBuilder pageBuilder = new PageBuilder(
            configBuilder
                .event(CASEWORKER_EDIT_DRAFT_ORDER)
                .forStates(CaseManagement, AwaitingHearing, AwaitingOutcome, CaseStayed, CaseClosed)
                .name("Orders: Edit draft")
                .showSummary()
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::draftUpdated)
                .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER)
                .grantHistoryOnly(SOLICITOR));
        editDraftOrder.addTo(pageBuilder);
        draftOrderEditMainContentPage.addTo(pageBuilder);
        editDraftOrderAddDocumentFooter(pageBuilder);
        previewOrder.addTo(pageBuilder);

    }

    private void editDraftOrderAddDocumentFooter(PageBuilder pageBuilder) {
        pageBuilder.page("editDraftOrderAddDocumentFooter", this::midEvent)
            .pageLabel("Document footer")
            .label("draftOrderDocFooter",
                "\nOrder Signature\n"
                    + "\nConfirm the Role and Surname of the person who made this order - this will be added"
                    + " to the bottom of the generated order notice. E.g. 'Tribunal Judge Farrelly'")
            .complex(CaseData::getDraftOrderContentCIC)
            .mandatory(DraftOrderContentCIC::getOrderSignature)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {

        var caseData = orderService.generateOrderFile(details.getData(), details.getId());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        CaseData caseData = details.getData();
        // Reset values so that they are not prepopulated when creating another draft order
        caseData.setDraftOrderContentCIC(new DraftOrderContentCIC());
        caseData.getCicCase().getDraftOrderDynamicList().setValue(null);
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(details.getState())
            .build();
    }

    public SubmittedCallbackResponse draftUpdated(CaseDetails<CaseData, State> details,
                                                  CaseDetails<CaseData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Draft order updated %n## Use "
                + "'Send order' to send the case documentation to parties in the case."))
            .build();
    }


}
