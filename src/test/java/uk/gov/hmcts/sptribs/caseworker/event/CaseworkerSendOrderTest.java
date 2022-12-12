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
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.model.DateModel;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.caseworker.model.OrderIssuingType;
import uk.gov.hmcts.sptribs.caseworker.model.ReminderDays;
import uk.gov.hmcts.sptribs.caseworker.model.YesNo;
import uk.gov.hmcts.sptribs.caseworker.service.OrderService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.caseworker.event.CaseworkerSendOrder.CASEWORKER_SEND_ORDER;
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


@ExtendWith(MockitoExtension.class)
class CaseworkerSendOrderTest {
    @InjectMocks
    private CaseworkerSendOrder caseworkerSendOrder;

    @Mock
    private OrderService orderService;

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
    void shouldRunAboutToStart() {
        //Given
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder().build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        updatedCaseDetails.setData(caseData);
        when(orderService.getDraftOrderDynamicList(any())).thenReturn(null);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerSendOrder.aboutToStart(updatedCaseDetails);

        //Then
        assertThat(response).isNotNull();
    }

    @Test
    void shouldSuccessfullySendOrderWithEmail() {
        //Given

        final DateModel dateModel = DateModel.builder().dueDate(LocalDate.now()).information("inf").build();
        final ListValue<DateModel> dates = new ListValue<>();
        dates.setValue(dateModel);
        final CicCase cicCase = CicCase.builder()
            .fullName(TEST_FIRST_NAME)
            .email(TEST_SUBJECT_EMAIL)
            .respondantEmail(TEST_CASEWORKER_USER_EMAIL)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .representativeEmailAddress(TEST_SOLICITOR_EMAIL)
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .orderIssuingType(OrderIssuingType.UPLOAD_A_NEW_ORDER_FROM_YOUR_COMPUTER)
            .orderDueDates(List.of(dates))
            .orderReminderYesOrNo(YesNo.YES)
            .orderReminderDays(ReminderDays.DAY_COUNT_1)
            .build();
        final CaseData caseData = caseData();
        caseData.setCicCase(cicCase);
        caseData.setDraftOrderCIC(DraftOrderCIC.builder().anOrderTemplate(OrderTemplate.CIC7_ME_DMI_REPORTS).build());
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
        assertThat(order.getUploadedFile()).isNull();
        assertThat(order.getDraftOrder()).isNull();
    }

    @Test
    void shouldSuccessfullySendOrderWithPost() {
        //Given

        final DateModel dateModel = DateModel.builder().dueDate(LocalDate.now()).information("inf").build();
        final ListValue<DateModel> dates = new ListValue<>();
        dates.setValue(dateModel);
        final CicCase cicCase = CicCase.builder()
            .fullName(TEST_FIRST_NAME)
            .address(SUBJECT_ADDRESS)
            .respondantEmail(TEST_CASEWORKER_USER_EMAIL)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .representativeAddress(SOLICITOR_ADDRESS)
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .orderIssuingType(OrderIssuingType.UPLOAD_A_NEW_ORDER_FROM_YOUR_COMPUTER)
            .orderDueDates(List.of(dates))
            .orderReminderYesOrNo(YesNo.YES)
            .orderReminderDays(ReminderDays.DAY_COUNT_1)
            .build();
        final CaseData caseData = caseData();
        caseData.setCicCase(cicCase);
        caseData.setDraftOrderCIC(DraftOrderCIC.builder().anOrderTemplate(OrderTemplate.CIC7_ME_DMI_REPORTS).build());
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
        assertThat(order.getUploadedFile()).isNull();
        assertThat(order.getDraftOrder()).isNull();
        assertThat(order.getReminderDay().getLabel()).isEqualTo(ReminderDays.DAY_COUNT_1.getLabel());
    }

    @Test
    void shouldSuccessfullySendOrderWithOnlyPost() {
        //Given

        final DateModel dateModel = DateModel.builder().dueDate(LocalDate.now()).information("inf").build();
        final ListValue<DateModel> dates = new ListValue<>();
        dates.setValue(dateModel);
        final CicCase cicCase = CicCase.builder()
            .fullName(TEST_FIRST_NAME)
            .address(SUBJECT_ADDRESS)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .representativeAddress(SOLICITOR_ADDRESS)
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .orderIssuingType(OrderIssuingType.UPLOAD_A_NEW_ORDER_FROM_YOUR_COMPUTER)
            .orderDueDates(List.of(dates))
            .orderReminderYesOrNo(YesNo.YES)
            .orderReminderDays(ReminderDays.DAY_COUNT_1)
            .build();
        final CaseData caseData = caseData();
        caseData.setCicCase(cicCase);
        caseData.setDraftOrderCIC(DraftOrderCIC.builder().anOrderTemplate(OrderTemplate.CIC7_ME_DMI_REPORTS).build());
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerSendOrder.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse sent = caseworkerSendOrder.sent(updatedCaseDetails, beforeDetails);
        AboutToStartOrSubmitResponse<CaseData, State> response2 =
            caseworkerSendOrder.aboutToSubmit(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(sent).isNotNull();
        assertThat(response).isNotNull();
        Order order = response.getData().getCicCase().getOrderList().get(0).getValue();
        assertThat(order.getDueDateList().get(0).getValue().getDueDate()).isNotNull();
        assertThat(order.getUploadedFile()).isNull();
        assertThat(order.getDraftOrder()).isNull();
        assertThat(order.getReminderDay().getLabel()).isEqualTo(ReminderDays.DAY_COUNT_1.getLabel());
    }

    @Test
    void shouldSuccessfullySendOrderMultiple() {
        //Given

        final DateModel dateModel = DateModel.builder().dueDate(LocalDate.now()).information("inf").build();
        final ListValue<DateModel> dates = new ListValue<>();
        dates.setValue(dateModel);
        final CicCase cicCase = CicCase.builder()
            .fullName(TEST_FIRST_NAME)
            .address(SUBJECT_ADDRESS)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .representativeAddress(SOLICITOR_ADDRESS)
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .orderIssuingType(OrderIssuingType.UPLOAD_A_NEW_ORDER_FROM_YOUR_COMPUTER)
            .orderDueDates(List.of(dates))
            .orderReminderYesOrNo(YesNo.YES)
            .orderReminderDays(ReminderDays.DAY_COUNT_1)
            .build();
        final CaseData caseData = caseData();
        caseData.setCicCase(cicCase);
        caseData.setDraftOrderCIC(DraftOrderCIC.builder().anOrderTemplate(OrderTemplate.CIC7_ME_DMI_REPORTS).build());
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerSendOrder.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse sent = caseworkerSendOrder.sent(updatedCaseDetails, beforeDetails);
        AboutToStartOrSubmitResponse<CaseData, State> response2 =
            caseworkerSendOrder.aboutToSubmit(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(sent).isNotNull();
        assertThat(response).isNotNull();
        assertThat(response2.getData().getCicCase().getOrderList()).hasSize(2);
        Order order = response.getData().getCicCase().getOrderList().get(0).getValue();
        assertThat(order.getDueDateList().get(0).getValue().getDueDate()).isNotNull();
        assertThat(order.getUploadedFile()).isNull();
        assertThat(order.getDraftOrder()).isNull();
    }
}
