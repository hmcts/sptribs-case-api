package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_EDIT_DRAFT_ORDER;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.DOUBLE_HYPHEN;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseStayed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.ReadyToList;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaseworkerEditDraftOrder implements CCDConfig<CaseData, State, UserRole> {

    private static final CcdPageConfiguration editDraftOrder = new EditDraftOrder();

    private static final CcdPageConfiguration previewOrder = new PreviewDraftOrder("previewEditOrder", CASEWORKER_EDIT_DRAFT_ORDER);

    private static final CcdPageConfiguration draftOrderEditMainContentPage = new DraftOrderMainContentPage();

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH);

    private final OrderService orderService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Event.EventBuilder<CaseData, UserRole, State> eventBuilder =
            configBuilder
                .event(CASEWORKER_EDIT_DRAFT_ORDER)
                .forStates(CaseManagement, ReadyToList, AwaitingHearing, CaseStayed, CaseClosed)
                .name("Orders: Edit draft")
                .showSummary()
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::submitted)
                .grant(CREATE_READ_UPDATE, SUPER_USER,
                     ST_CIC_HEARING_CENTRE_ADMIN, ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_WA_CONFIG_USER)
                .publishToCamunda();

        PageBuilder pageBuilder = new PageBuilder(eventBuilder);
        editDraftOrder.addTo(pageBuilder);
        draftOrderEditMainContentPage.addTo(pageBuilder);
        editDraftOrderAddDocumentFooter(pageBuilder);
        previewOrder.addTo(pageBuilder);
    }

    private void editDraftOrderAddDocumentFooter(PageBuilder pageBuilder) {
        pageBuilder.page("editDraftOrderAddDocumentFooter", this::midEvent)
            .pageLabel("Document footer")
            .label("draftOrderDocFooter",
                """

                    Order Signature

                    Confirm the Role and Surname of the person who made this order - this will be added to the bottom of the generated \
                    order notice. E.g. 'Tribunal Judge Farrelly'""")
            .complex(CaseData::getDraftOrderContentCIC)
            .mandatory(DraftOrderContentCIC::getOrderSignature)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {

        Calendar cal = Calendar.getInstance();
        String date = simpleDateFormat.format(cal.getTime());
        final CaseData caseData = orderService.generateOrderFile(details.getData(), details.getId(), date);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {

        CaseData caseData = details.getData();
        DynamicList dynamicList = caseData.getCicCase().getDraftOrderDynamicList();
        UUID code = dynamicList.getValue().getCode();
        String label = dynamicList.getValue().getLabel();
        String[] dynamicListLabel = label.split(DOUBLE_HYPHEN);
        String editedFileName = caseData.getCicCase().getOrderTemplateIssued().getFilename();
        String[] fileNameFields = editedFileName.split(DOUBLE_HYPHEN);
        String date = fileNameFields[2].substring(0, fileNameFields[2].length() - 4);
        for (DynamicListElement element : dynamicList.getListItems()) {
            if (element.getCode().equals(code)) {
                element.setLabel(dynamicListLabel[0] + DOUBLE_HYPHEN + date + DOUBLE_HYPHEN + "draft.pdf");
                break;
            }
        }
        for (ListValue<DraftOrderCIC> draftOrderCIC : caseData.getCicCase().getDraftOrderCICList()) {
            String[] draftOrderFile = draftOrderCIC.getValue()
                .getTemplateGeneratedDocument().getFilename().split(DOUBLE_HYPHEN);
            if (label
                .contains(draftOrderCIC.getValue().getDraftOrderContentCIC().getOrderTemplate().getLabel())
                && draftOrderFile[2].contains(dynamicListLabel[1])) {
                draftOrderCIC.getValue().setTemplateGeneratedDocument(caseData.getCicCase().getOrderTemplateIssued());
                draftOrderCIC.getValue().setDraftOrderContentCIC(caseData.getDraftOrderContentCIC());
            }
        }
        caseData.setDraftOrderContentCIC(new DraftOrderContentCIC());
        caseData.getCicCase().getDraftOrderDynamicList().setValue(null);
        caseData.getCicCase().setOrderTemplateIssued(null);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(details.getState())
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                                  CaseDetails<CaseData, State> beforeDetails) {

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Draft order updated %n## Use "
                + "'Send order' to send the case documentation to parties in the case."))
            .build();
    }
}
