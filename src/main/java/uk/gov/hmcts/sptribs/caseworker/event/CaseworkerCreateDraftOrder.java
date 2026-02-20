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
import uk.gov.hmcts.sptribs.taskmanagement.TaskManagementService;
import uk.gov.hmcts.sptribs.taskmanagement.TaskType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CREATE_DRAFT_ORDER;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.DOUBLE_HYPHEN;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseStayed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.ReadyToList;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_TEAM_LEADER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_JUDGE;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.createDueDate;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.issueDueDate;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.processCaseWithdrawalDirections;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.processCaseWithdrawalDirectionsListed;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.processCorrections;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.processDirectionsReListedCase;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.processDirectionsReListedCaseWithin5Days;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.processDirectionsReturned;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.processListingDirections;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.processListingDirectionsListed;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.processOtherDirectionsReturned;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.processPostponementDirections;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.processReinstatementDecisionNotice;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.processRule27Decision;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.processRule27DecisionListed;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.processSetAsideDirections;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.processStayDirections;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.processStayDirectionsListed;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.processStrikeOutDirectionsReturned;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.processTimeExtensionDirectionsReturned;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.processWrittenReasons;
import static uk.gov.hmcts.sptribs.taskmanagement.TaskType.reviewOrder;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaseworkerCreateDraftOrder implements CCDConfig<CaseData, State, UserRole> {

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH);
    private static final List<TaskType> COMPLETABLE_TASKS = Stream
        .of(TaskType.values())
        .filter(taskType -> taskType.name().startsWith("review")
            && !taskType.name().startsWith("reviewSpecificAccess")
            && taskType != reviewOrder)
        .toList();
    private static final String WITHDRAWAL_REQUEST = "Withdrawal request";
    private static final String RULE_27_REQUEST = "Rule 27 request";
    private static final String LISTING_DIRECTIONS = "Listing directions";
    private static final String LISTED_CASE = "Listed case";
    private static final String LISTED_CASE_WITHIN_5_DAYS = "Listed case (within 5 days)";
    private static final String SET_ASIDE_REQUEST = "Set aside request";
    private static final String CORRECTIONS = "Corrections";
    private static final String NEW_CASE = "New case";
    private static final String POSTPONEMENT_REQUEST = "Postponement request";
    private static final String TIME_EXTENSION_REQUEST = "Time extension request";
    private static final String REINSTATEMENT_REQUEST = "Reinstatement request";
    private static final String OTHER = "Other";
    private static final String WRITTEN_REASONS_REQUEST = "Written reasons request";
    private static final String STRIKE_OUT_REQUEST = "Strike out request";
    private static final String STAY_REQUEST = "Stay request";

    private static final CcdPageConfiguration createDraftOrder = new CreateDraftOrder();
    private static final CcdPageConfiguration draftOrderMainContentPage = new DraftOrderMainContentPage();
    private static final CcdPageConfiguration previewOrder = new PreviewDraftOrder("previewDraftOrderPage", CASEWORKER_CREATE_DRAFT_ORDER);

    private final OrderService orderService;
    private final TaskManagementService taskManagementService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Event.EventBuilder<CaseData, UserRole, State> eventBuilder =
            configBuilder
                .event(CASEWORKER_CREATE_DRAFT_ORDER)
                .forStates(CaseManagement, ReadyToList, AwaitingHearing, CaseStayed, CaseClosed)
                .name("Orders: Create draft")
                .description("Orders: Create draft")
                .showSummary()
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::submitted)
                .grant(CREATE_READ_UPDATE, SUPER_USER,
                    ST_CIC_HEARING_CENTRE_ADMIN, ST_CIC_HEARING_CENTRE_TEAM_LEADER, ST_CIC_WA_CONFIG_USER)
                .grantHistoryOnly(ST_CIC_CASEWORKER, ST_CIC_SENIOR_CASEWORKER, ST_CIC_JUDGE, ST_CIC_SENIOR_JUDGE);

        PageBuilder pageBuilder = new PageBuilder(eventBuilder);
        createDraftOrder.addTo(pageBuilder);
        draftOrderMainContentPage.addTo(pageBuilder);
        createDraftOrderAddDocumentFooter(pageBuilder);
        previewOrder.addTo(pageBuilder);

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

        caseData.getCicCase().setOrderTemplateIssued(null);

        taskManagementService.enqueueCompletionTasks(
            Stream.concat(COMPLETABLE_TASKS.stream(), Stream.of(createDueDate)).toList(),
            details.getId()
        );
        taskManagementService.enqueueInitiationTasks(
            getInitiationTaskTypes(details.getState(), caseData),
            caseData,
            details.getId()
        );

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

    private List<TaskType> getInitiationTaskTypes(State state, CaseData caseData) {
        String referralType = caseData.getCicCase().getReferralTypeForWA();
        if (state == CaseManagement && (referralType == null || referralType.isBlank())) {
            return List.of(issueDueDate);
        }

        if (WITHDRAWAL_REQUEST.equals(referralType)) {
            if (state == AwaitingHearing) {
                return List.of(processCaseWithdrawalDirectionsListed);
            }
            if (state == CaseManagement || state == ReadyToList) {
                return List.of(processCaseWithdrawalDirections);
            }
        }

        if (RULE_27_REQUEST.equals(referralType)) {
            if (state == AwaitingHearing) {
                return List.of(processRule27DecisionListed);
            }
            if (state == CaseManagement || state == ReadyToList) {
                return List.of(processRule27Decision);
            }
        }

        if (LISTING_DIRECTIONS.equals(referralType)) {
            if (state == CaseManagement) {
                return List.of(processListingDirections);
            }
            if (state == ReadyToList) {
                return List.of(processListingDirectionsListed);
            }
        }

        if (LISTED_CASE.equals(referralType) && state == AwaitingHearing) {
            return List.of(processDirectionsReListedCase);
        }

        if (LISTED_CASE_WITHIN_5_DAYS.equals(referralType) && state == AwaitingHearing) {
            return List.of(processDirectionsReListedCaseWithin5Days);
        }

        if (SET_ASIDE_REQUEST.equals(referralType) && state == CaseClosed) {
            return List.of(processSetAsideDirections);
        }

        if (CORRECTIONS.equals(referralType) && state == CaseClosed) {
            return List.of(processCorrections);
        }

        if (NEW_CASE.equals(referralType) && (state == CaseManagement || state == ReadyToList)) {
            return List.of(processDirectionsReturned);
        }

        if (POSTPONEMENT_REQUEST.equals(referralType) && state == AwaitingHearing) {
            return List.of(processPostponementDirections);
        }

        if (TIME_EXTENSION_REQUEST.equals(referralType) && (state == CaseManagement || state == ReadyToList)) {
            return List.of(processTimeExtensionDirectionsReturned);
        }

        if (REINSTATEMENT_REQUEST.equals(referralType) && state == CaseClosed) {
            return List.of(processReinstatementDecisionNotice);
        }

        if (OTHER.equals(referralType)) {
            return List.of(processOtherDirectionsReturned);
        }

        if (WRITTEN_REASONS_REQUEST.equals(referralType) && state == CaseClosed) {
            return List.of(processWrittenReasons);
        }

        if (STRIKE_OUT_REQUEST.equals(referralType) && (state == CaseManagement || state == ReadyToList)) {
            return List.of(processStrikeOutDirectionsReturned);
        }

        if (STAY_REQUEST.equals(referralType)) {
            if (state == AwaitingHearing) {
                return List.of(processStayDirectionsListed);
            }
            if (state == CaseManagement || state == ReadyToList) {
                return List.of(processStayDirections);
            }
        }

        return List.of();
    }
}
