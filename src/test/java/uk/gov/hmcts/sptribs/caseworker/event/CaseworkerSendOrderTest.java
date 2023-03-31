package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.model.DateModel;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderContentCIC;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.caseworker.model.OrderIssuingType;
import uk.gov.hmcts.sptribs.caseworker.model.ReminderDays;
import uk.gov.hmcts.sptribs.caseworker.model.YesNo;
import uk.gov.hmcts.sptribs.caseworker.util.MessageUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.notification.NewOrderIssuedNotification;
import uk.gov.hmcts.sptribs.document.model.CICDocument;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBJECT_ADDRESS;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASEWORKER_USER_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SUBJECT_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCICDocumentList;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_SEND_ORDER;


@ExtendWith(MockitoExtension.class)
class CaseworkerSendOrderTest {

    @InjectMocks
    private CaseworkerSendOrder caseworkerSendOrder;

    @Mock
    private NewOrderIssuedNotification newOrderIssuedNotification;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseworkerSendOrder.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_SEND_ORDER);
    }


    @Test
    void shouldSuccessfullySendOrderWithEmail() {
        //Given

        final DateModel dateModel = DateModel.builder().dueDate(LocalDate.now()).information("inf").build();
        final ListValue<DateModel> dates = new ListValue<>();
        dates.setValue(dateModel);
        final DraftOrderCIC draftOrderCIC = DraftOrderCIC.builder()
            .draftOrderContentCIC(DraftOrderContentCIC.builder().orderTemplate(OrderTemplate.CIC6_GENERAL_DIRECTIONS).build())
            .build();
        final ListValue<DraftOrderCIC> draftOrderCICListValue = new ListValue<>();
        draftOrderCICListValue.setValue(draftOrderCIC);
        draftOrderCICListValue.setId("0");

        final UUID uuid = UUID.randomUUID();
        final CICDocument document = CICDocument.builder()
            .documentLink(Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build())
            .documentEmailContent("content")
            .build();
        ListValue<CICDocument> documentListValue = new ListValue<>();
        documentListValue.setValue(document);

        final CicCase cicCase = CicCase.builder()
            .draftOrderDynamicList(getDraftOrderList())
            .draftOrderCICList(List.of(draftOrderCICListValue))
            .fullName(TEST_FIRST_NAME)
            .email(TEST_SUBJECT_EMAIL)
            .contactPreferenceType(ContactPreferenceType.EMAIL)
            .respondentEmail(TEST_CASEWORKER_USER_EMAIL)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .representativeEmailAddress(TEST_SOLICITOR_EMAIL)
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .representativeContactDetailsPreference(ContactPreferenceType.EMAIL)
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .orderIssuingType(OrderIssuingType.UPLOAD_A_NEW_ORDER_FROM_YOUR_COMPUTER)
            .orderFile(List.of(documentListValue))
            .orderDueDates(List.of(dates))
            .orderReminderYesOrNo(YesNo.YES)
            .orderReminderDays(ReminderDays.DAY_COUNT_1)
            .orderIssuingType(OrderIssuingType.ISSUE_AND_SEND_AN_EXISTING_DRAFT)
            .build();
        final CaseData caseData = caseData();
        caseData.setCicCase(cicCase);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerSendOrder.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse sent = caseworkerSendOrder.sent(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(sent).isNotNull();
        assertThat(response).isNotNull();
        Order order = response.getData().getCicCase().getOrderList().get(0).getValue();
        assertThat(order.getDueDateList().get(0).getValue().getDueDate()).isNotNull();
        assertThat(order.getUploadedFile()).isNotNull();
    }

    @Test
    void shouldSuccessfullySendOrderWithPost() {
        //Given

        final DateModel dateModel = DateModel.builder().dueDate(LocalDate.now()).information("inf").build();
        final ListValue<DateModel> dates = new ListValue<>();
        dates.setValue(dateModel);
        final DraftOrderCIC draftOrderCIC = DraftOrderCIC.builder()
            .draftOrderContentCIC(DraftOrderContentCIC.builder().orderTemplate(OrderTemplate.CIC6_GENERAL_DIRECTIONS).build())
            .build();
        final ListValue<DraftOrderCIC> draftOrderCICListValue = new ListValue<>();
        draftOrderCICListValue.setValue(draftOrderCIC);
        draftOrderCICListValue.setId("0");

        List<ListValue<CICDocument>> documentList = getCICDocumentList();

        final CicCase cicCase = CicCase.builder()
            .draftOrderCICList(List.of(draftOrderCICListValue))
            .fullName(TEST_FIRST_NAME)
            .address(SUBJECT_ADDRESS)
            .contactPreferenceType(ContactPreferenceType.POST)
            .respondentEmail(TEST_CASEWORKER_USER_EMAIL)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .representativeAddress(SOLICITOR_ADDRESS)
            .representativeContactDetailsPreference(ContactPreferenceType.POST)
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .orderIssuingType(OrderIssuingType.ISSUE_AND_SEND_AN_EXISTING_DRAFT)
            .orderDueDates(List.of(dates))
            .orderReminderYesOrNo(YesNo.YES)
            .orderReminderDays(ReminderDays.DAY_COUNT_1)
            .draftOrderDynamicList(getDraftOrderList())
            .orderFile(documentList)
            .build();
        final CaseData caseData = caseData();
        caseData.setCicCase(cicCase);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerSendOrder.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse sent = caseworkerSendOrder.sent(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(sent).isNotNull();
        assertThat(response).isNotNull();
        Order order = response.getData().getCicCase().getOrderList().get(0).getValue();
        assertThat(order.getDueDateList().get(0).getValue().getDueDate()).isNotNull();
        assertThat(order.getUploadedFile()).isNotNull();
        assertThat(order.getReminderDay().getLabel()).isEqualTo(ReminderDays.DAY_COUNT_1.getLabel());
    }

    @Test
    void shouldSuccessfullySendOrderWithOnlyPost() {
        //Given

        final DateModel dateModel = DateModel.builder().dueDate(LocalDate.now()).information("inf").build();
        final ListValue<DateModel> dates = new ListValue<>();
        dates.setValue(dateModel);
        final DraftOrderCIC draftOrderCIC = DraftOrderCIC.builder()
            .draftOrderContentCIC(DraftOrderContentCIC.builder().orderTemplate(OrderTemplate.CIC6_GENERAL_DIRECTIONS).build())
            .build();
        final ListValue<DraftOrderCIC> draftOrderCICListValue = new ListValue<>();
        draftOrderCICListValue.setValue(draftOrderCIC);
        draftOrderCICListValue.setId("0");

        List<ListValue<CICDocument>> documentList = getCICDocumentList();

        final CicCase cicCase = CicCase.builder()
            .draftOrderCICList(List.of(draftOrderCICListValue))
            .draftOrderDynamicList(getDraftOrderList())
            .fullName(TEST_FIRST_NAME)
            .address(SUBJECT_ADDRESS)
            .contactPreferenceType(ContactPreferenceType.POST)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .representativeAddress(SOLICITOR_ADDRESS)
            .representativeContactDetailsPreference(ContactPreferenceType.POST)
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .orderIssuingType(OrderIssuingType.ISSUE_AND_SEND_AN_EXISTING_DRAFT)
            .orderDueDates(List.of(dates))
            .orderReminderYesOrNo(YesNo.YES)
            .orderReminderDays(ReminderDays.DAY_COUNT_1)
            .orderFile(documentList)
            .build();
        final CaseData caseData = caseData();
        caseData.setCicCase(cicCase);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerSendOrder.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse sent = caseworkerSendOrder.sent(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(sent).isNotNull();
        assertThat(response).isNotNull();
        Order order = response.getData().getCicCase().getOrderList().get(0).getValue();
        assertThat(order.getDueDateList().get(0).getValue().getDueDate()).isNotNull();
        assertThat(order.getUploadedFile()).isNotNull();
        assertThat(order.getReminderDay().getLabel()).isEqualTo(ReminderDays.DAY_COUNT_1.getLabel());
    }

    @Test
    void shouldSuccessfullySendOrderMultiple() {
        //Given

        final DateModel dateModel = DateModel.builder().dueDate(LocalDate.now()).information("inf").build();
        final ListValue<DateModel> dates = new ListValue<>();
        dates.setValue(dateModel);
        final DraftOrderCIC firstDraftOrder = DraftOrderCIC.builder()
            .draftOrderContentCIC(DraftOrderContentCIC.builder().orderTemplate(OrderTemplate.CIC7_ME_DMI_REPORTS).build())
            .build();
        final ListValue<DraftOrderCIC> firstValue = new ListValue<>();
        firstValue.setValue(firstDraftOrder);
        firstValue.setId("0");
        final DraftOrderCIC secondDraftOrder = DraftOrderCIC.builder()
            .draftOrderContentCIC(DraftOrderContentCIC.builder().orderTemplate(OrderTemplate.CIC6_GENERAL_DIRECTIONS).build())
            .build();

        final ListValue<DraftOrderCIC> secondValue = new ListValue<>();
        secondValue.setValue(secondDraftOrder);
        secondValue.setId("1");
        List<ListValue<DraftOrderCIC>> draftOrderList = new ArrayList<>();
        draftOrderList.add(firstValue);
        draftOrderList.add(secondValue);

        List<ListValue<CICDocument>> documentList = getCICDocumentList();

        final CicCase cicCase = CicCase.builder()
            .draftOrderCICList(draftOrderList)
            .draftOrderDynamicList(getDraftOrderList())
            .fullName(TEST_FIRST_NAME)
            .contactPreferenceType(ContactPreferenceType.POST)
            .address(SUBJECT_ADDRESS)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .representativeAddress(SOLICITOR_ADDRESS)
            .representativeContactDetailsPreference(ContactPreferenceType.POST)
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .orderIssuingType(OrderIssuingType.ISSUE_AND_SEND_AN_EXISTING_DRAFT)
            .orderDueDates(List.of(dates))
            .orderReminderYesOrNo(YesNo.YES)
            .orderReminderDays(ReminderDays.DAY_COUNT_1)
            .orderFile(documentList)
            .build();
        final CaseData caseData = caseData();
        caseData.setCicCase(cicCase);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerSendOrder.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse sent = caseworkerSendOrder.sent(updatedCaseDetails, beforeDetails);
        updatedCaseDetails.setData(caseData);
        AboutToStartOrSubmitResponse<CaseData, State> response2 =
            caseworkerSendOrder.aboutToSubmit(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(sent).isNotNull();
        assertThat(response).isNotNull();
        assertThat(response2.getData().getCicCase().getOrderList()).hasSize(2);
    }


    @Test
    void shouldSetSendOrderTemplatesFromSelectedDraftOrder() {
        // Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final DynamicListElement element = DynamicListElement.builder()
            .code(UUID.randomUUID())
            .build();
        final List<DynamicListElement> elements = new ArrayList<>();
        elements.add(element);
        final DynamicList dynamicList = DynamicList.builder()
            .listItems(elements)
            .value(element)
            .build();
        final List<ListValue<DraftOrderCIC>> draftOrderCICList = new ArrayList<>();
        DraftOrderContentCIC contentCIC = DraftOrderContentCIC.builder()
            .mainContent("content")
            .orderSignature("signature")
            .orderTemplate(OrderTemplate.CIC6_GENERAL_DIRECTIONS)
            .build();
        DraftOrderCIC orderCIC = DraftOrderCIC.builder()
            .draftOrderContentCIC(contentCIC)
            .build();
        ListValue<DraftOrderCIC> listValue = ListValue.<DraftOrderCIC>builder()
            .value(orderCIC)
            .build();
        draftOrderCICList.add(listValue);

        List<ListValue<CICDocument>> documentList = getCICDocumentList();

        final CicCase cicCase = CicCase.builder()
            .draftOrderDynamicList(dynamicList)
            .draftOrderCICList(draftOrderCICList)
            .orderFile(documentList)
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);

        // When
        caseworkerSendOrder.aboutToSubmit(caseDetails, caseDetails);

        // Then

        assertThat(caseDetails.getData().getCicCase().getDraftOrderDynamicList().getListItems()
            .contains(OrderTemplate.CIC6_GENERAL_DIRECTIONS));
    }

    @Test
    void shouldSendOrderSuccessFully() {
        // Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder()
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);

        // When
        caseworkerSendOrder.sent(caseDetails, beforeDetails);
        SubmittedCallbackResponse s = SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Order sent %n## %s",
                MessageUtil.generateSimpleMessage(cicCase)))
            .build();
        // Then
        assertThat(s.getConfirmationHeader().contains("# Order sent %n## %s"));
    }

    private DynamicList getDraftOrderList() {
        final DynamicListElement listItem = DynamicListElement
            .builder()
            .label(OrderTemplate.CIC6_GENERAL_DIRECTIONS.getLabel())
            .code(UUID.randomUUID())
            .build();
        return DynamicList
            .builder()
            .value(listItem)
            .listItems(List.of(listItem))
            .build();
    }
}
