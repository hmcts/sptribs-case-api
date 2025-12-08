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
import uk.gov.hmcts.sptribs.ciccase.model.ApplicantCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.access.Permissions;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.notification.dispatcher.NewOrderIssuedNotification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.COLON;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.DOUBLE_HYPHEN;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.DRAFT;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.SENT;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
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
    void shouldAddPublishToCamundaWhenWAIsEnabled() {

        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerSendOrder.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_SEND_ORDER);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::isPublishToCamunda)
                .contains(true);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::getGrants)
                .extracting(map -> map.containsKey(ST_CIC_WA_CONFIG_USER))
                .contains(true);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::getGrants)
                .extracting(map -> map.get(ST_CIC_WA_CONFIG_USER))
                .contains(Permissions.CREATE_READ_UPDATE);
    }

    @Test
    void shouldSuccessfullySendOrderWithEmail() {
        //Given
        final DateModel dateModel = DateModel.builder().dueDate(LocalDate.now()).information("inf").build();
        final ListValue<DateModel> dates = new ListValue<>();
        dates.setValue(dateModel);
        final DraftOrderCIC draftOrderCIC = DraftOrderCIC.builder()
            .templateGeneratedDocument(Document.builder().filename("aa--bb--cc").build())
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
        final ListValue<CICDocument> documentListValue = new ListValue<>();
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
            .notifyPartyApplicant(Set.of(ApplicantCIC.APPLICANT_CIC))
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
        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerSendOrder.aboutToSubmit(updatedCaseDetails, beforeDetails);
        final SubmittedCallbackResponse submitted = caseworkerSendOrder.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(submitted).isNotNull();
        assertThat(response).isNotNull();
        final Order order = response.getData().getCicCase().getOrderList().getFirst().getValue();
        assertThat(order.getDueDateList().getFirst().getValue().getDueDate()).isNotNull();
        assertThat(order.getUploadedFile()).isNotNull();
    }

    @Test
    void shouldSuccessfullyUpdateFileNameForDraftOrderWhenDraftColonPrefix() {
        //Given
        final DateModel dateModel = DateModel.builder().dueDate(LocalDate.now()).information("inf").build();
        final ListValue<DateModel> dates = new ListValue<>();
        dates.setValue(dateModel);
        final DraftOrderCIC draftOrderCIC = DraftOrderCIC.builder()
            .templateGeneratedDocument(Document.builder().filename(DRAFT + COLON + "aa--bb--14-10-2025 13:00:00.pdf").build())
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
        final ListValue<CICDocument> documentListValue = new ListValue<>();
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
            .notifyPartyApplicant(Set.of(ApplicantCIC.APPLICANT_CIC))
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
        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerSendOrder.aboutToSubmit(updatedCaseDetails, beforeDetails);
        final SubmittedCallbackResponse submitted = caseworkerSendOrder.submitted(updatedCaseDetails, beforeDetails);

        //Then
        final Order order = response.getData().getCicCase().getOrderList().getFirst().getValue();
        assertThat(order.getDraftOrder().getTemplateGeneratedDocument().getFilename())
            .isNotNull();
        assertThat(order.getDraftOrder().getTemplateGeneratedDocument().getFilename())
            .isEqualTo(SENT + COLON + "aa--bb--14-10-2025 13:00:00.pdf");
        assertThat(submitted.getConfirmationHeader()).contains("Order sent");
    }

    @Test
    void shouldSuccessfullyUpdateFileNameForDraftOrderWhenDraftColonPrefixIsNotPresent() {
        //Given
        final DateModel dateModel = DateModel.builder().dueDate(LocalDate.now()).information("inf").build();
        final ListValue<DateModel> dates = new ListValue<>();
        dates.setValue(dateModel);
        final DraftOrderCIC draftOrderCIC = DraftOrderCIC.builder()
            .templateGeneratedDocument(Document.builder().filename("aa--bb--14-10-2025 13:00:00.pdf").build())
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
        final ListValue<CICDocument> documentListValue = new ListValue<>();
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
            .notifyPartyApplicant(Set.of(ApplicantCIC.APPLICANT_CIC))
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
        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerSendOrder.aboutToSubmit(updatedCaseDetails, beforeDetails);
        final SubmittedCallbackResponse submitted = caseworkerSendOrder.submitted(updatedCaseDetails, beforeDetails);

        //Then
        Order order = response.getData().getCicCase().getOrderList().getFirst().getValue();
        assertThat(order.getDraftOrder().getTemplateGeneratedDocument().getFilename())
            .isNotNull();
        assertThat(order.getDraftOrder().getTemplateGeneratedDocument().getFilename())
            .isEqualTo(SENT + COLON + "aa--bb--14-10-2025 13:00:00.pdf");
        assertThat(submitted.getConfirmationHeader()).contains("Order sent");
    }

    @Test
    void shouldSuccessfullySendOrderWithPost() {
        //Given
        final DateModel dateModel = DateModel.builder().dueDate(LocalDate.now()).information("inf").build();
        final ListValue<DateModel> dates = new ListValue<>();
        dates.setValue(dateModel);
        final DraftOrderCIC draftOrderCIC = DraftOrderCIC.builder()
            .templateGeneratedDocument(Document.builder().filename("aa--bb--14-10-2025 13:00:00.pdf").build())
            .draftOrderContentCIC(DraftOrderContentCIC.builder().orderTemplate(OrderTemplate.CIC6_GENERAL_DIRECTIONS).build())
            .build();
        final ListValue<DraftOrderCIC> draftOrderCICListValue = new ListValue<>();
        draftOrderCICListValue.setValue(draftOrderCIC);
        draftOrderCICListValue.setId("0");

        final List<ListValue<CICDocument>> documentList = getCICDocumentList("file.pdf");

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
        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerSendOrder.aboutToSubmit(updatedCaseDetails, beforeDetails);
        final SubmittedCallbackResponse submitted = caseworkerSendOrder.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(submitted.getConfirmationHeader()).contains("Order sent");
        assertThat(response).isNotNull();
        Order order = response.getData().getCicCase().getOrderList().getFirst().getValue();
        assertThat(order.getDueDateList().getFirst().getValue().getDueDate()).isNotNull();
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
            .templateGeneratedDocument(Document.builder().filename("aa--bb--14-10-2025 13:00:00.pdf").build())
            .draftOrderContentCIC(DraftOrderContentCIC.builder().orderTemplate(OrderTemplate.CIC6_GENERAL_DIRECTIONS).build())
            .build();
        final ListValue<DraftOrderCIC> draftOrderCICListValue = new ListValue<>();
        draftOrderCICListValue.setValue(draftOrderCIC);
        draftOrderCICListValue.setId("0");

        final List<ListValue<CICDocument>> documentList = getCICDocumentList("file.pdf");

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
        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerSendOrder.aboutToSubmit(updatedCaseDetails, beforeDetails);
        final SubmittedCallbackResponse submitted = caseworkerSendOrder.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(submitted.getConfirmationHeader()).contains("Order sent");
        assertThat(response).isNotNull();
        Order order = response.getData().getCicCase().getOrderList().getFirst().getValue();
        assertThat(order.getDueDateList().getFirst().getValue().getDueDate()).isNotNull();
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
            .templateGeneratedDocument(Document.builder().filename("aa--bb--cc").build())
            .draftOrderContentCIC(DraftOrderContentCIC.builder().orderTemplate(OrderTemplate.CIC7_ME_DMI_REPORTS).build())
            .build();
        final ListValue<DraftOrderCIC> firstValue = new ListValue<>();
        firstValue.setValue(firstDraftOrder);
        firstValue.setId("0");
        final DraftOrderCIC secondDraftOrder = DraftOrderCIC.builder()
            .templateGeneratedDocument(Document.builder().filename("aa--bb--cc").build())
            .draftOrderContentCIC(DraftOrderContentCIC.builder().orderTemplate(OrderTemplate.CIC6_GENERAL_DIRECTIONS).build())
            .build();

        final ListValue<DraftOrderCIC> secondValue = new ListValue<>();
        secondValue.setValue(secondDraftOrder);
        secondValue.setId("1");
        final List<ListValue<DraftOrderCIC>> draftOrderList = new ArrayList<>();
        draftOrderList.add(firstValue);
        draftOrderList.add(secondValue);

        final List<ListValue<CICDocument>> documentList = getCICDocumentList("file.pdf");

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
        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerSendOrder.aboutToSubmit(updatedCaseDetails, beforeDetails);
        final SubmittedCallbackResponse submitted = caseworkerSendOrder.submitted(updatedCaseDetails, beforeDetails);
        updatedCaseDetails.setData(caseData);
        final AboutToStartOrSubmitResponse<CaseData, State> response2 =
            caseworkerSendOrder.aboutToSubmit(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(submitted.getConfirmationHeader()).contains("Order sent");
        assertThat(response).isNotNull();
        assertThat(response2.getData().getCicCase().getOrderList()).hasSize(2);
    }

    @Test
    void aboutToStartShouldSetCurrentEvent() {
        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
                .data(CaseData.builder().build())
                .build();
        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerSendOrder.aboutToStart(caseDetails);

        assertThat(response).isNotNull();
        assertThat(response.getData().getCurrentEvent()).isEqualTo(CASEWORKER_SEND_ORDER);
    }

    @Test
    void shouldOnlySendOneOrderWhenMultipleDraftsExist() {
        final DateModel dateModel = DateModel.builder().dueDate(LocalDate.now()).information("inf").build();
        final ListValue<DateModel> dates = new ListValue<>();
        dates.setValue(dateModel);
        DraftOrderContentCIC draftOrderContentCIC = DraftOrderContentCIC.builder()
                .orderTemplate(OrderTemplate.CIC3_RULE_27)
                .mainContent("main content")
                .orderSignature("test signature")
                .build();
        Document document = Document.builder()
                .filename("DRAFT :Order--[test]--14-10-2025 13:00:00.pdf")
                .binaryUrl("http://test.url/binary")
                .url("http://test.url")
                .build();
        DraftOrderCIC draftOrderCIC = DraftOrderCIC.builder()
                .draftOrderContentCIC(draftOrderContentCIC)
                .templateGeneratedDocument(document)
                .build();
        final ListValue<DraftOrderCIC> draftOrderCICListValue = new ListValue<>();
        draftOrderCICListValue.setValue(draftOrderCIC);
        draftOrderCICListValue.setId("0");

        DraftOrderContentCIC draftOrderContentCIC2 = DraftOrderContentCIC.builder()
                .orderTemplate(OrderTemplate.CIC3_RULE_27)
                .mainContent("main content")
                .orderSignature("test signature")
                .build();
        Document document2 = Document.builder()
                .filename("DRAFT :Order--[test]--15-10-2025 11:00:00.pdf")
                .binaryUrl("http://test.url.a/binary")
                .url("http://test.url.a")
                .build();
        DraftOrderCIC draftOrderCIC2 = DraftOrderCIC.builder()
                .draftOrderContentCIC(draftOrderContentCIC2)
                .templateGeneratedDocument(document2)
                .build();

        final ListValue<DraftOrderCIC> draftOrderCICListValue2 = new ListValue<>();
        draftOrderCICListValue2.setId("1");
        draftOrderCICListValue2.setValue(draftOrderCIC2);

        final UUID uuid = UUID.randomUUID();
        final CICDocument cicDocument = CICDocument.builder()
                .documentLink(Document.builder().binaryUrl("http://url/" + uuid).url("http://url/" + uuid).build())
                .documentEmailContent("content")
                .build();
        final ListValue<CICDocument> documentListValue = new ListValue<>();
        documentListValue.setValue(cicDocument);

        final CicCase cicCase = CicCase.builder()
                .draftOrderDynamicList(getMultipleDraftOrderDynamicList())
                .draftOrderCICList(List.of(draftOrderCICListValue, draftOrderCICListValue2))
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
                .notifyPartyApplicant(Set.of(ApplicantCIC.APPLICANT_CIC))
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
        final AboutToStartOrSubmitResponse<CaseData, State> response =
                caseworkerSendOrder.aboutToSubmit(updatedCaseDetails, beforeDetails);
        final SubmittedCallbackResponse submitted = caseworkerSendOrder.submitted(updatedCaseDetails, beforeDetails);

        //Then
        final Order order = response.getData().getCicCase().getOrderList().getFirst().getValue();
        assertThat(order.getDraftOrder().getTemplateGeneratedDocument().getFilename())
                .isNotNull();
        assertThat(order.getDraftOrder().getTemplateGeneratedDocument().getFilename())
                .isEqualTo(SENT + COLON + "Order--[test]--15-10-2025 11:00:00.pdf");
        assertThat(submitted.getConfirmationHeader()).contains("Order sent");
        final List<DraftOrderCIC> draftOrderCICS =
                response.getData().getCicCase().getDraftOrderCICList().stream().map(ListValue::getValue).toList();
        assertThat(draftOrderCICS).hasSize(1);
        final DraftOrderCIC expectedRemainingDraft = draftOrderCICS.getFirst();
        assertThat(expectedRemainingDraft).isEqualTo(draftOrderCIC);
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
        final DraftOrderContentCIC contentCIC = DraftOrderContentCIC.builder()
            .mainContent("content")
            .orderSignature("signature")
            .orderTemplate(OrderTemplate.CIC6_GENERAL_DIRECTIONS)
            .build();
        final DraftOrderCIC orderCIC = DraftOrderCIC.builder()
            .draftOrderContentCIC(contentCIC)
            .build();
        final ListValue<DraftOrderCIC> listValue = ListValue.<DraftOrderCIC>builder()
            .value(orderCIC)
            .build();
        draftOrderCICList.add(listValue);

        final List<ListValue<CICDocument>> documentList = getCICDocumentList("file.pdf");

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
        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerSendOrder.aboutToSubmit(caseDetails, caseDetails);

        // Then
        assertThat(response.getData().getCicCase().getDraftOrderCICList()).isEqualTo(draftOrderCICList);
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
        caseworkerSendOrder.submitted(caseDetails, beforeDetails);
        final SubmittedCallbackResponse s = SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Order sent %n## %s",
                MessageUtil.generateSimpleMessage(cicCase)))
            .build();

        // Then
        assertThat(s.getConfirmationHeader()).contains("# Order sent");
    }

    private DynamicList getDraftOrderList() {
        String date = "14-10-2025 13:00:00.pdf";
        final DynamicListElement listItem = DynamicListElement
            .builder()
            .label(OrderTemplate.CIC6_GENERAL_DIRECTIONS.getLabel() + DOUBLE_HYPHEN + date)
            .code(UUID.randomUUID())
            .build();
        return DynamicList
            .builder()
            .value(listItem)
            .listItems(List.of(listItem))
            .build();
    }

    private DynamicList getMultipleDraftOrderDynamicList() {
        String date = "14-10-2025 13:00:00.pdf";
        String date2 = "15-10-2025 11:00:00.pdf";
        final DynamicListElement listElement = DynamicListElement
                .builder()
                .label(OrderTemplate.CIC3_RULE_27.getLabel() + DOUBLE_HYPHEN + date)
                .code(UUID.randomUUID())
                .build();
        final DynamicListElement listElement2 = DynamicListElement
                .builder()
                .label(OrderTemplate.CIC3_RULE_27.getLabel() + DOUBLE_HYPHEN + date2)
                .code(UUID.randomUUID())
                .build();
        return DynamicList
                .builder()
                .value(listElement2)
                .listItems(List.of(listElement, listElement2))
                .build();
    }
}
