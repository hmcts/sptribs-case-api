package uk.gov.hmcts.sptribs.notification.dispatcher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.caseworker.model.ReinstateReason;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.common.CommonConstants;
import uk.gov.hmcts.sptribs.document.DocumentUtil;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.hmcts.sptribs.notification.model.Party;
import uk.gov.hmcts.sptribs.testutil.TestDataHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_ORDER;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class NewOrderIssuedNotificationTest {
    @Mock
    private NotificationServiceCIC notificationService;

    @Mock
    private NotificationHelper notificationHelper;

    @InjectMocks
    private NewOrderIssuedNotification newOrderIssuedNotification;

    private static final NotificationResponse NOTIFICATION_RESPONSE = NotificationResponse.builder().id("123").build();

    @Captor
    private ArgumentCaptor<Map<String, String>> templateDocumentVarsCaptor;

    private CaseData caseData;
    private Order sentOrder;

    @BeforeEach
    void setUp() {
        caseData = new CaseData();
        CicCase cicCase = CicCase.builder().fullName("fullName").caseNumber(TEST_CASE_ID.toString()).notifyPartyMessage("message").build();
        caseData.setCicCase(cicCase);

        sentOrder = Order.builder().uploadedFile(TestDataHelper.getCICDocumentList("test.pdf")).build();
        ListValue<Order> orderListValue = new ListValue<>();
        orderListValue.setValue(sentOrder);
        cicCase.setOrderList(List.of(orderListValue));
    }

    @Nested
    class WhenCitizenDashboardDisabled {
        @BeforeEach
        void setUpFlags() {
            ReflectionTestUtils.setField(newOrderIssuedNotification, "citizenDashboardEnabled", false);
        }

        @Test
        void shouldNotifySubjectOfNewOrderIssuedWithEmail() {
            //Given
            caseData.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
            caseData.getCicCase().setEmail("testSubject@outlook.com");

            //When
            when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getSubjectCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), any(Party.class))).thenReturn(
                NOTIFICATION_RESPONSE);

            String correspondenceId = newOrderIssuedNotification.sendToSubject(caseData, TEST_CASE_ID.toString());

            //Then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE.getId());
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.SUBJECT));
            verify(notificationHelper).buildEmailNotificationRequest(
                eq(caseData.getCicCase().getEmail()),
                eq(new ArrayList<>()),
                templateDocumentVarsCaptor.capture(),
                eq(new HashMap<>()),
                eq(TemplateName.NEW_ORDER_ISSUED_EMAIL));

            Map<String, String> actualTemplateDocumentVars = templateDocumentVarsCaptor.getValue();
            String orderUUID = DocumentUtil.getDocumentUuidFromCICDocument(sentOrder.getUploadedFile().getFirst().getValue());
            assertThat(actualTemplateDocumentVars).containsEntry(TRIBUNAL_ORDER, orderUUID);
        }

        @Test
        void shouldNotifySubjectOfNewOrderIssuedWithEmailWithUploadedTemplateDocument() {
            //Given
            caseData.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
            caseData.getCicCase().setEmail("testSubject@outlook.com");

            final Order recentOrder = Order.builder().uploadedFile(TestDataHelper.getCICDocumentList("test.pdf")).build();
            final Order oldOrder = Order.builder().uploadedFile(TestDataHelper.getCICDocumentList("old-test.doc")).build();
            ListValue<Order> orderListValue1 = new ListValue<>();
            ListValue<Order> orderListValue2 = new ListValue<>();
            orderListValue1.setValue(recentOrder);
            orderListValue2.setValue(oldOrder);
            caseData.getCicCase().setOrderList(List.of(orderListValue1, orderListValue2));

            //When
            when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getSubjectCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), any(Party.class))).thenReturn(
                NOTIFICATION_RESPONSE);

            String correspondenceId = newOrderIssuedNotification.sendToSubject(caseData, TEST_CASE_ID.toString());

            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE.getId());
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.SUBJECT));
            verify(notificationHelper).buildEmailNotificationRequest(eq(caseData.getCicCase().getEmail()),
                eq(new ArrayList<>()),
                templateDocumentVarsCaptor.capture(),
                eq(new HashMap<>()),
                eq(TemplateName.NEW_ORDER_ISSUED_EMAIL));

            Map<String, String> actualTemplateDocumentVars = templateDocumentVarsCaptor.getValue();
            String expectedOrderUUID = DocumentUtil.getDocumentUuidFromCICDocument(recentOrder.getUploadedFile().getFirst().getValue());
            assertThat(actualTemplateDocumentVars).containsEntry(TRIBUNAL_ORDER, expectedOrderUUID);

        }

        @Test
        void shouldNotifySubjectOfNewOrderIssuedWithPost() {
            //Given
            caseData.getCicCase().setContactPreferenceType(ContactPreferenceType.POST);
            caseData.getCicCase().setAddress(AddressGlobalUK.builder().build());
            caseData.getCicCase().setReinstateReason(ReinstateReason.OTHER);

            //When
            when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getSubjectCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            doNothing().when(notificationHelper).addAddressTemplateVars(any(AddressGlobalUK.class), anyMap());
            when(notificationService.sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()))).thenReturn(
                NOTIFICATION_RESPONSE);

            String correspondenceId = newOrderIssuedNotification.sendToSubject(caseData, TEST_CASE_ID.toString());

            //Then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE.getId());
            verify(notificationService).sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
            verify(notificationHelper).buildLetterNotificationRequest(
                new HashMap<>(),
                TemplateName.NEW_ORDER_ISSUED_POST);
        }

        @Test
        void shouldNotifyRespondentOfNewOrderIssuedWithEmail() {
            //Given
            caseData.getCicCase().setRespondentName("respondentName");
            caseData.getCicCase().setRespondentEmail("testRespondent@outlook.com");

            final Order recentOrder = Order.builder().uploadedFile(TestDataHelper.getCICDocumentList("test.doc")).build();
            final Order oldOrder = Order.builder().uploadedFile(TestDataHelper.getCICDocumentList("test-old.pdf")).build();
            final ListValue<Order> orderListValue1 = new ListValue<>();
            final ListValue<Order> orderListValue2 = new ListValue<>();
            orderListValue1.setValue(recentOrder);
            orderListValue2.setValue(oldOrder);
            caseData.getCicCase().setOrderList(List.of(orderListValue1, orderListValue2));

            //When
            when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getRespondentCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), any(Party.class))).thenReturn(
                NOTIFICATION_RESPONSE);

            String correspondenceId = newOrderIssuedNotification.sendToRespondent(caseData, TEST_CASE_ID.toString());

            //Then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE.getId());
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.RESPONDENT));
            verify(notificationHelper).buildEmailNotificationRequest(eq(caseData.getCicCase().getRespondentEmail()),
                eq(new ArrayList<>()),
                templateDocumentVarsCaptor.capture(),
                eq(new HashMap<>()),
                eq(TemplateName.NEW_ORDER_ISSUED_EMAIL));

            Map<String, String> actualTemplateDocumentVars = templateDocumentVarsCaptor.getValue();
            assertThat(actualTemplateDocumentVars).containsEntry(TRIBUNAL_ORDER,
                DocumentUtil.getDocumentUuidFromCICDocument(recentOrder.getUploadedFile().getFirst().getValue()));
        }

        @Test
        void shouldNotifyRepresentativeOfNewOrderIssuedWithEmail() {
            //Given
            caseData.getCicCase().setRepresentativeFullName("repFullName");
            caseData.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.EMAIL);
            caseData.getCicCase().setRepresentativeEmailAddress("testrepr@outlook.com");
            caseData.getCicCase().setReinstateReason(ReinstateReason.OTHER);

            final DraftOrderCIC draftOrderCIC = DraftOrderCIC.builder()
                .templateGeneratedDocument(TestDataHelper.getDocumentWithBinary("test-draft.pdf"))
                .build();
            final Order recentOrder = Order.builder().draftOrder(draftOrderCIC).build();
            final Order oldOrder = Order.builder().uploadedFile(TestDataHelper.getCICDocumentList("old-order.pdf")).build();
            final ListValue<Order> orderListValue1 = new ListValue<>();
            final ListValue<Order> orderListValue2 = new ListValue<>();
            orderListValue1.setValue(recentOrder);
            orderListValue2.setValue(oldOrder);
            caseData.getCicCase().setOrderList(List.of(orderListValue1, orderListValue2));

            //When
            when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getRepresentativeCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), any(Party.class))).thenReturn(
                NOTIFICATION_RESPONSE);

            String correspondenceId = newOrderIssuedNotification.sendToRepresentative(caseData, TEST_CASE_ID.toString());

            //Then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE.getId());
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.REPRESENTATIVE));
            verify(notificationHelper).buildEmailNotificationRequest(eq(caseData.getCicCase().getRepresentativeEmailAddress()),
                eq(new ArrayList<>()),
                templateDocumentVarsCaptor.capture(),
                eq(new HashMap<>()),
                eq(TemplateName.NEW_ORDER_ISSUED_EMAIL));

            Map<String, String> actualTemplateDocumentVars = templateDocumentVarsCaptor.getValue();
            assertThat(actualTemplateDocumentVars).containsEntry(TRIBUNAL_ORDER,
                DocumentUtil.getUuid(recentOrder.getDraftOrder().getTemplateGeneratedDocument()));
        }

        @Test
        void shouldNotifyRepresentativeOfNewOrderIssuedWithPost() {
            //Given
            caseData.getCicCase().setRepresentativeFullName("repFullName");
            caseData.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.POST);
            caseData.getCicCase().setRepresentativeAddress(AddressGlobalUK.builder().build());

            //When
            when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getRepresentativeCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            doNothing().when(notificationHelper).addAddressTemplateVars(any(), anyMap());
            when(notificationService.sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()))).thenReturn(
                NOTIFICATION_RESPONSE);

            String correspondenceId = newOrderIssuedNotification.sendToRepresentative(caseData, TEST_CASE_ID.toString());

            //Then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE.getId());
            verify(notificationService).sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
            verify(notificationHelper).buildLetterNotificationRequest(
                new HashMap<>(),
                TemplateName.NEW_ORDER_ISSUED_POST);
        }

        @Test
        void shouldNotifyApplicantOfNewOrderIssuedWithEmail() {
            //Given
            caseData.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
            caseData.getCicCase().setApplicantEmailAddress("testapplicant@outlook.com");
            caseData.getCicCase().setReinstateReason(ReinstateReason.OTHER);
            caseData.getCicCase().setOrderList(List.of());

            final Order recentOrder = Order.builder().uploadedFile(null).build();
            final Order oldOrder = Order.builder().uploadedFile(TestDataHelper.getCICDocumentList("test.docx")).build();
            final ListValue<Order> orderListValue1 = new ListValue<>();
            final ListValue<Order> orderListValue2 = new ListValue<>();
            orderListValue1.setValue(recentOrder);
            orderListValue2.setValue(oldOrder);
            caseData.getCicCase().setOrderList(List.of(orderListValue1, orderListValue2));

            //When
            when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getApplicantCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), any(Party.class))).thenReturn(
                NOTIFICATION_RESPONSE);

            String correspondenceId = newOrderIssuedNotification.sendToApplicant(caseData, TEST_CASE_ID.toString());

            //Then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE.getId());
            verify(notificationHelper).buildEmailNotificationRequest(eq(caseData.getCicCase().getApplicantEmailAddress()),
                eq(new ArrayList<>()),
                templateDocumentVarsCaptor.capture(),
                eq(new HashMap<>()),
                eq(TemplateName.NEW_ORDER_ISSUED_EMAIL));
            assertThat(templateDocumentVarsCaptor.getValue().get(TRIBUNAL_ORDER)).isNull();
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.APPLICANT));
        }

        @Test
        void shouldNotifyApplicantOfNewOrderIssuedWithEmailWithNoUploadedDocument() {
            //Given
            caseData.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
            caseData.getCicCase().setApplicantEmailAddress("testapplicant@outlook.com");
            caseData.getCicCase().setReinstateReason(ReinstateReason.OTHER);
            caseData.getCicCase().setOrderList(List.of());

            //When
            when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getApplicantCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), any(Party.class))).thenReturn(
                NOTIFICATION_RESPONSE);

            String correspondenceId = newOrderIssuedNotification.sendToApplicant(caseData, TEST_CASE_ID.toString());

            //Then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE.getId());
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.APPLICANT));
            verify(notificationHelper).buildEmailNotificationRequest(
                eq(caseData.getCicCase().getApplicantEmailAddress()),
                eq(new ArrayList<>()),
                templateDocumentVarsCaptor.capture(),
                eq(new HashMap<>()),
                eq(TemplateName.NEW_ORDER_ISSUED_EMAIL));

            Map<String, String> actualTemplateDocumentVars = templateDocumentVarsCaptor.getValue();
            assertThat(actualTemplateDocumentVars).isEmpty();
        }

        @Test
        void shouldNotifyApplicantOfNewOrderIssuedWithPost() {
            //Given
            caseData.getCicCase().setContactPreferenceType(ContactPreferenceType.POST);
            caseData.getCicCase().setAddress(AddressGlobalUK.builder().build());
            caseData.getCicCase().setReinstateReason(ReinstateReason.OTHER);
            caseData.getCicCase().setOrderList(List.of());

            //When
            when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getApplicantCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            doNothing().when(notificationHelper).addAddressTemplateVars(any(AddressGlobalUK.class), anyMap());
            when(notificationService.sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()))).thenReturn(
                NOTIFICATION_RESPONSE);

            String correspondenceId = newOrderIssuedNotification.sendToApplicant(caseData, TEST_CASE_ID.toString());

            //Then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE.getId());
            verify(notificationService).sendLetter(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
            verify(notificationHelper).buildLetterNotificationRequest(
                new HashMap<>(),
                TemplateName.NEW_ORDER_ISSUED_POST);
        }
    }

    @Nested
    class WhenCitizenDashboardEnabled {

        @Captor
        private ArgumentCaptor<Map<String, Object>> templateVarsCaptor;

        @BeforeEach
        void setUpFlags() {
            ReflectionTestUtils.setField(newOrderIssuedNotification, "citizenDashboardEnabled", true);
            ReflectionTestUtils.setField(newOrderIssuedNotification, "citizenDashboardUrl", "https://frontend.url/dashboard");
        }

        @Test
        void shouldNotifySubjectOfNewOrderIssuedWithEmail() {
            //Given
            caseData.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
            caseData.getCicCase().setEmail("testSubject@outlook.com");

            //When
            when(notificationHelper.buildEmailNotificationRequest(anyString(), anyList(), anyMap(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
            when(notificationHelper.getSubjectCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());
            when(notificationService.sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), any(Party.class))).thenReturn(
                NOTIFICATION_RESPONSE);

            String correspondenceId = newOrderIssuedNotification.sendToSubject(caseData, TEST_CASE_ID.toString());

            //Then
            assertThat(correspondenceId).isEqualTo(NOTIFICATION_RESPONSE.getId());
            verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()), eq(Party.SUBJECT));
            verify(notificationHelper).buildEmailNotificationRequest(
                eq(caseData.getCicCase().getEmail()),
                eq(new ArrayList<>()),
                templateDocumentVarsCaptor.capture(),
                templateVarsCaptor.capture(),
                eq(TemplateName.NEW_ORDER_ISSUED_EMAIL_NEW_CD));

            Map<String, String> actualTemplateDocumentVars = templateDocumentVarsCaptor.getValue();
            String orderUUID = DocumentUtil.getDocumentUuidFromCICDocument(sentOrder.getUploadedFile().getFirst().getValue());
            assertThat(actualTemplateDocumentVars).containsEntry(TRIBUNAL_ORDER, orderUUID);

            Map<String, Object> actualTemplateVars = templateVarsCaptor.getValue();
            assertThat(actualTemplateVars).containsEntry(CommonConstants.DASHBOARD_KEY, "https://frontend.url/dashboard");
        }
    }
}
