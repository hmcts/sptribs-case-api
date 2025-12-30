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
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.event.page.CreateDraftOrder;
import uk.gov.hmcts.sptribs.common.event.page.DraftOrderMainContentPage;
import uk.gov.hmcts.sptribs.common.event.page.PreviewDraftOrder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CREATE_DRAFT_ORDER;
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
public class CaseworkerCreateDraftOrder implements CCDConfig<CaseData, State, UserRole> {

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH);

    private static final CcdPageConfiguration createDraftOrder = new CreateDraftOrder();
    private static final CcdPageConfiguration draftOrderMainContentPage = new DraftOrderMainContentPage();
    private static final CcdPageConfiguration previewOrder = new PreviewDraftOrder();

    private final OrderService orderService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Event.EventBuilder<CaseData, UserRole, State> eventBuilder =
            configBuilder
                .event(CASEWORKER_CREATE_DRAFT_ORDER)
                .forStates(CaseManagement, ReadyToList, AwaitingHearing, CaseStayed, CaseClosed)
                .name("Orders: Create draft")
                .description("Orders: Create draft")
                .showSummary()
                .aboutToStartCallback(this::aboutToStart)
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::submitted)
                .grant(CREATE_READ_UPDATE, SUPER_USER,
                    ST_CIC_HEARING_CENTRE_ADMIN, ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_WA_CONFIG_USER)
                .publishToCamunda();

        PageBuilder pageBuilder = new PageBuilder(eventBuilder);
        createDraftOrder.addTo(pageBuilder);
        draftOrderMainContentPage.addTo(pageBuilder);
        createDraftOrderAddDocumentFooter(pageBuilder);
        previewOrder.addTo(pageBuilder);

    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> caseDetails) {
        CaseData data = caseDetails.getData();
        data.setCurrentEvent(CASEWORKER_CREATE_DRAFT_ORDER);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        Calendar cal = Calendar.getInstance();
        String date = simpleDateFormat.format(cal.getTime());
        final CaseData caseData = orderService.generateOrderFile(details.getData(), details.getId(), date);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        final CaseData caseData = details.getData();
        final OrderTemplate orderTemplate = caseData.getDraftOrderContentCIC().getOrderTemplate();

        String[] fileName = caseData.getCicCase().getOrderTemplateIssued().getFilename().split(DOUBLE_HYPHEN);
        addToDraftOrderTemplatesDynamicList(orderTemplate, caseData.getCicCase(), fileName[2]);

        DraftOrderCIC draftOrderCIC = DraftOrderCIC.builder()
            .draftOrderContentCIC(caseData.getDraftOrderContentCIC())
            .templateGeneratedDocument(caseData.getCicCase().getOrderTemplateIssued())
            .build();

        caseData.setDraftOrderContentCIC(new DraftOrderContentCIC());

        if (isEmpty(caseData.getCicCase().getDraftOrderCICList())) {
            final List<ListValue<DraftOrderCIC>> listValues = new ArrayList<>();

            final ListValue<DraftOrderCIC> listValue = ListValue
                .<DraftOrderCIC>builder()
                .id("1")
                .value(draftOrderCIC)
                .build();

            listValues.add(listValue);

            caseData.getCicCase().setDraftOrderCICList(listValues);
        } else {
            AtomicInteger listValueIndex = new AtomicInteger(0);
            ListValue<DraftOrderCIC> listValue = ListValue
                .<DraftOrderCIC>builder()
                .value(draftOrderCIC)
                .build();

            caseData.getCicCase().getDraftOrderCICList().addFirst(listValue);

            caseData.getCicCase().getDraftOrderCICList().forEach(
                draftOrderListValue -> draftOrderListValue.setId(String.valueOf(listValueIndex.incrementAndGet())));

        }

        caseData.setCurrentEvent("");
        caseData.getCicCase().setOrderTemplateIssued(null);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .state(details.getState())
            .data(caseData)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Draft order created.")
            .build();
    }

    private void createDraftOrderAddDocumentFooter(PageBuilder pageBuilder) {
        pageBuilder.page("createDraftOrderAddDocumentFooter", this::midEvent)
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

    private void addToDraftOrderTemplatesDynamicList(final OrderTemplate orderTemplate, CicCase cicCase, String date) {
        DynamicList orderTemplateDynamicList = cicCase.getDraftOrderDynamicList();
        if (orderTemplateDynamicList == null) {
            orderTemplateDynamicList = DynamicList.builder().listItems(new ArrayList<>()).build();
            cicCase.setDraftOrderDynamicList(orderTemplateDynamicList);
        }

        String templateNamePlusCurrentDate = orderTemplate.getLabel() + DOUBLE_HYPHEN + date + DOUBLE_HYPHEN + "draft.pdf";

        DynamicListElement element = DynamicListElement.builder().label(templateNamePlusCurrentDate).code(UUID.randomUUID()).build();
        orderTemplateDynamicList.getListItems().add(element);
    }
}
