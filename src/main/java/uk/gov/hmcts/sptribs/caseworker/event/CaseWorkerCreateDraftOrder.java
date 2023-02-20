package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
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
public class CaseWorkerCreateDraftOrder implements CCDConfig<CaseData, State, UserRole> {

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH);

    private static final CcdPageConfiguration createDraftOrder = new CreateDraftOrder();
    private static final CcdPageConfiguration draftOrderMainContentPage = new DraftOrderMainContentPage();
    private static final CcdPageConfiguration previewOrder = new PreviewDraftOrder();

    @Autowired
    private OrderService orderService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(
            configBuilder
                .event(CASEWORKER_CREATE_DRAFT_ORDER)
                .forStates(CaseManagement, AwaitingHearing, AwaitingOutcome, CaseStayed, CaseClosed)
                .name("Orders: Create draft")
                .description("Orders: Create draft")
                .showSummary()
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::draftCreated)
                .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER)
                .grantHistoryOnly(SOLICITOR));
        createDraftOrder.addTo(pageBuilder);
        draftOrderMainContentPage.addTo(pageBuilder);
        createDraftOrderAddDocumentFooter(pageBuilder);
        previewOrder.addTo(pageBuilder);

    }

    private void createDraftOrderAddDocumentFooter(PageBuilder pageBuilder) {
        pageBuilder.page("createDraftOrderAddDocumentFooter", this::midEvent)
            .pageLabel("Document footer")
            .label("draftOrderDocFooter",
                "\nOrder Signature\n"
                    + "\nConfirm the Role and Surname of the person who made this order - this will be added"
                    + " to the bottom of the generated order notice. E.g. 'Tribunal Judge Farrelly'")
            .complex(CaseData::getDraftOrderContentCIC)
            .mandatory(DraftOrderContentCIC::getOrderSignature)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        var caseData = details.getData();
        OrderTemplate orderTemplate = caseData.getDraftOrderContentCIC().getOrderTemplate();

        DynamicListElement addedElement = addToDraftOrderTemplatesDynamicList(orderTemplate, caseData.getCicCase());
        UUID code = addedElement.getCode();
        DraftOrderCIC draftOrderCIC = DraftOrderCIC.builder()
            .code(code.toString())
            .draftOrderContentCIC(caseData.getDraftOrderContentCIC())
            .template(orderTemplate)
            .templateGeneratedDocument(caseData.getCicCase().getOrderTemplateIssued())
            .build();
        caseData.setDraftOrderContentCIC(new DraftOrderContentCIC());
        if (isEmpty(caseData.getCicCase().getDraftOrderCICList())) {
            List<ListValue<DraftOrderCIC>> listValues = new ArrayList<>();

            var listValue = ListValue
                .<DraftOrderCIC>builder()
                .id("1")
                .value(draftOrderCIC)
                .build();

            listValues.add(listValue);

            caseData.getCicCase().setDraftOrderCICList(listValues);
        } else {
            AtomicInteger listValueIndex = new AtomicInteger(0);
            var listValue = ListValue
                .<DraftOrderCIC>builder()
                .value(draftOrderCIC)
                .build();

            caseData.getCicCase().getDraftOrderCICList().add(0, listValue);

            caseData.getCicCase().getDraftOrderCICList().forEach(
                draftOrderListValue -> draftOrderListValue.setId(String.valueOf(listValueIndex.incrementAndGet())));

        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .state(details.getState())
            .data(caseData)
            .build();
    }

    private DynamicListElement addToDraftOrderTemplatesDynamicList(final OrderTemplate orderTemplate, CicCase cicCase) {
        DynamicList orderTemplateDynamicList = cicCase.getDraftOrderDynamicList();
        if (orderTemplateDynamicList == null) {
            orderTemplateDynamicList = DynamicList.builder().listItems(new ArrayList<>()).build();
            cicCase.setDraftOrderDynamicList(orderTemplateDynamicList);
        }

        Calendar cal = Calendar.getInstance();
        String templateNamePlusCurrentDate = orderTemplate.getLabel() + " " + simpleDateFormat.format(cal.getTime()) + "_draft.pdf";

        DynamicListElement element = DynamicListElement.builder().label(templateNamePlusCurrentDate).code(UUID.randomUUID()).build();
        orderTemplateDynamicList.getListItems().add(element);
        return element;
    }

    public SubmittedCallbackResponse draftCreated(CaseDetails<CaseData, State> details,
                                                  CaseDetails<CaseData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Draft order created. ")
            .build();
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
}
