package uk.gov.hmcts.sptribs.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.caseworker.model.ReinstateReason;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_ORDER;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCICDocumentList;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCICDocumentListWithUrl;

@ExtendWith(MockitoExtension.class)
public class NewOrderIssuedNotificationTest {
    @Mock
    private NotificationServiceCIC notificationService;

    @Mock
    private NotificationHelper notificationHelper;

    @InjectMocks
    private NewOrderIssuedNotification newOrderIssuedNotification;

    @Test
    void shouldNotifySubjectWithEmail() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
        data.getCicCase().setEmail("testrepr@outlook.com");

        Order order = Order.builder().uploadedFile(getCICDocumentList("test.pdf")).build();
        ListValue<Order> orderListValue = new ListValue<>();
        orderListValue.setValue(order);
        data.getCicCase().setOrderList(List.of(orderListValue));

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getSubjectCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());

        newOrderIssuedNotification.sendToSubject(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldNotifySubjectWithEmailWithUploadedTemplateDocument() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
        data.getCicCase().setEmail("testrepr@outlook.com");

        Order recentOrder = Order.builder().uploadedFile(getCICDocumentListWithUrl("test.pdf", "http://url/test.pdf")).build();
        Order oldOrder = Order.builder().uploadedFile(getCICDocumentListWithUrl("test.doc", "http://url/test.doc")).build();
        ListValue<Order> orderListValue1 = new ListValue<>();
        ListValue<Order> orderListValue2 = new ListValue<>();
        orderListValue1.setValue(recentOrder);
        orderListValue2.setValue(oldOrder);
        data.getCicCase().setOrderList(List.of(orderListValue1, orderListValue2));

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getSubjectCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());

        newOrderIssuedNotification.sendToSubject(data, "CN1");

        //Then
        ArgumentCaptor<Map<String, String>> argument = ArgumentCaptor.forClass(Map.class);
        verify(notificationHelper).buildEmailNotificationRequest(eq("testrepr@outlook.com"), eq(true),
            argument.capture(), eq(new HashMap<>()), eq(TemplateName.NEW_ORDER_ISSUED_EMAIL));
        assertThat(argument.getValue())
            .containsEntry(TRIBUNAL_ORDER, recentOrder.getUploadedFile().get(0).getValue().getDocumentLink().getFilename());
        verify(notificationService).sendEmail(any(NotificationRequest.class));
    }

    @Test
    void shouldNotifySubjectWithEmailThrowsException() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
        data.getCicCase().setEmail("testrepr@outlook.com");
        data.getCicCase().setReinstateReason(ReinstateReason.OTHER);

        Order order = Order.builder().uploadedFile(getCICDocumentList("test.pdf")).build();
        ListValue<Order> orderListValue = new ListValue<>();
        orderListValue.setValue(order);
        data.getCicCase().setOrderList(List.of(orderListValue));

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getSubjectCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());

        newOrderIssuedNotification.sendToSubject(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
    }

    @Test
    void shouldNotifySubjectWithPost() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.POST);
        data.getCicCase().setAddress(AddressGlobalUK.builder().build());
        data.getCicCase().setReinstateReason(ReinstateReason.OTHER);

        //When
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getSubjectCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        doNothing().when(notificationHelper).addAddressTemplateVars(any(AddressGlobalUK.class), anyMap());
        newOrderIssuedNotification.sendToSubject(data, "CN1");

        //Then
        verify(notificationService).sendLetter(any(NotificationRequest.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldNotifyRespondentWithEmail() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setRespondentName("respondentName");
        data.getCicCase().setRespondentEmail("testrepr@outlook.com");

        Order recentOrder = Order.builder().uploadedFile(getCICDocumentListWithUrl("test.doc", "http://url/test.doc")).build();
        Order oldOrder = Order.builder().uploadedFile(getCICDocumentListWithUrl("test.pdf", "http://url/test.pdf")).build();
        ListValue<Order> orderListValue1 = new ListValue<>();
        ListValue<Order> orderListValue2 = new ListValue<>();
        orderListValue1.setValue(recentOrder);
        orderListValue2.setValue(oldOrder);
        data.getCicCase().setOrderList(List.of(orderListValue1, orderListValue2));

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRespondentCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        newOrderIssuedNotification.sendToRespondent(data, "CN1");

        //Then
        ArgumentCaptor<Map<String, String>> argument = ArgumentCaptor.forClass(Map.class);
        verify(notificationHelper).buildEmailNotificationRequest(eq("testrepr@outlook.com"), eq(true),
            argument.capture(), eq(new HashMap<>()), eq(TemplateName.NEW_ORDER_ISSUED_EMAIL));
        assertThat(argument.getValue())
            .containsEntry(TRIBUNAL_ORDER, recentOrder.getUploadedFile().get(0).getValue().getDocumentLink().getFilename());
        verify(notificationService).sendEmail(any(NotificationRequest.class));
    }

    @Test
    void shouldNotifyRespondentWithEmailWithException() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setRespondentName("respondentName");
        data.getCicCase().setRespondentEmail("testrepr@outlook.com");
        data.getCicCase().setReinstateReason(ReinstateReason.OTHER);

        Order order = Order.builder().uploadedFile(getCICDocumentList("test.pdf")).build();
        ListValue<Order> orderListValue = new ListValue<>();
        orderListValue.setValue(order);
        data.getCicCase().setOrderList(List.of(orderListValue));

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRespondentCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        newOrderIssuedNotification.sendToRespondent(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldNotifyRepresentativeWithEmail() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setRepresentativeFullName("repFullName");
        data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.EMAIL);
        data.getCicCase().setRepresentativeEmailAddress("testrepr@outlook.com");
        data.getCicCase().setReinstateReason(ReinstateReason.OTHER);

        Document document = new Document("http://url/test.tif", "test.tif", "http://url/test.tif");
        DraftOrderCIC draftOrderCIC = DraftOrderCIC.builder().templateGeneratedDocument(document).build();
        Order recentOrder = Order.builder().draftOrder(draftOrderCIC).build();
        Order oldOrder = Order.builder().uploadedFile(getCICDocumentListWithUrl("test.txt", "http://url/test.txt")).build();
        ListValue<Order> orderListValue1 = new ListValue<>();
        ListValue<Order> orderListValue2 = new ListValue<>();
        orderListValue1.setValue(recentOrder);
        orderListValue2.setValue(oldOrder);
        data.getCicCase().setOrderList(List.of(orderListValue1, orderListValue2));

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRepresentativeCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());

        newOrderIssuedNotification.sendToRepresentative(data, "CN1");

        //Then
        ArgumentCaptor<Map<String, String>> argument = ArgumentCaptor.forClass(Map.class);
        verify(notificationHelper).buildEmailNotificationRequest(eq("testrepr@outlook.com"), eq(true),
            argument.capture(), eq(new HashMap<>()), eq(TemplateName.NEW_ORDER_ISSUED_EMAIL));
        assertThat(argument.getValue())
            .containsEntry(TRIBUNAL_ORDER, recentOrder.getDraftOrder().getTemplateGeneratedDocument().getFilename());
        verify(notificationService).sendEmail(any(NotificationRequest.class));
    }

    @Test
    void shouldNotifyRepresentativeWithEmailWithException() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setRepresentativeFullName("repFullName");
        data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.EMAIL);
        data.getCicCase().setRepresentativeEmailAddress("testrepr@outlook.com");
        data.getCicCase().setReinstateReason(ReinstateReason.OTHER);

        Order order = Order.builder().uploadedFile(getCICDocumentList("test.pdf")).build();
        ListValue<Order> orderListValue = new ListValue<>();
        orderListValue.setValue(order);
        data.getCicCase().setOrderList(List.of(orderListValue));

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRepresentativeCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());

        newOrderIssuedNotification.sendToRepresentative(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
    }

    @Test
    void shouldNotifyRepresentativeWithPost() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setRepresentativeFullName("repFullName");
        data.getCicCase().setRepresentativeContactDetailsPreference(ContactPreferenceType.POST);
        data.getCicCase().setRepresentativeAddress(AddressGlobalUK.builder().build());

        //When
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRepresentativeCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        doNothing().when(notificationHelper).addAddressTemplateVars(any(), anyMap());
        newOrderIssuedNotification.sendToRepresentative(data, "CN1");

        //Then
        verify(notificationService).sendLetter(any(NotificationRequest.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldNotifyApplicantWithEmail() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
        data.getCicCase().setApplicantEmailAddress("testapplicant@outlook.com");
        data.getCicCase().setReinstateReason(ReinstateReason.OTHER);
        data.getCicCase().setOrderList(List.of());

        Order recentOrder = Order.builder().uploadedFile(null).build();
        Order oldOrder = Order.builder().uploadedFile(getCICDocumentListWithUrl("test.docx", "http://url/test.docx")).build();
        ListValue<Order> orderListValue1 = new ListValue<>();
        ListValue<Order> orderListValue2 = new ListValue<>();
        orderListValue1.setValue(recentOrder);
        orderListValue2.setValue(oldOrder);
        data.getCicCase().setOrderList(List.of(orderListValue1, orderListValue2));

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getApplicantCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());

        newOrderIssuedNotification.sendToApplicant(data, "CN1");

        //Then
        ArgumentCaptor<Map<String, String>> argument = ArgumentCaptor.forClass(Map.class);
        verify(notificationHelper).buildEmailNotificationRequest(eq("testapplicant@outlook.com"), eq(true),
            argument.capture(), eq(new HashMap<>()), eq(TemplateName.NEW_ORDER_ISSUED_EMAIL));
        assertThat(argument.getValue().get(TRIBUNAL_ORDER)).isNull();
        verify(notificationService).sendEmail(any(NotificationRequest.class));
    }

    @Test
    void shouldNotifyApplicantWithEmailWithNoUploadedDocument() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
        data.getCicCase().setApplicantEmailAddress("testapplicant@outlook.com");
        data.getCicCase().setReinstateReason(ReinstateReason.OTHER);
        data.getCicCase().setOrderList(List.of());

        Order order = Order.builder().uploadedFile(getCICDocumentList("test.pdf")).build();
        ListValue<Order> orderListValue = new ListValue<>();
        orderListValue.setValue(order);

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getApplicantCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());

        newOrderIssuedNotification.sendToApplicant(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
    }

    @Test
    void shouldNotifyApplicantWithEmailThrowsException() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.EMAIL);
        data.getCicCase().setApplicantEmailAddress("testapplicant@outlook.com");
        data.getCicCase().setReinstateReason(ReinstateReason.OTHER);
        data.getCicCase().setOrderList(List.of());

        Order order = Order.builder().uploadedFile(getCICDocumentList("test.pdf")).build();
        ListValue<Order> orderListValue = new ListValue<>();
        orderListValue.setValue(order);

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyBoolean(), anyMap(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getApplicantCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());

        newOrderIssuedNotification.sendToApplicant(data, "CN1");

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class));
    }

    @Test
    void shouldNotifyApplicantWithPost() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setContactPreferenceType(ContactPreferenceType.POST);
        data.getCicCase().setAddress(AddressGlobalUK.builder().build());
        data.getCicCase().setReinstateReason(ReinstateReason.OTHER);
        data.getCicCase().setOrderList(List.of());

        //When
        when(notificationHelper.buildLetterNotificationRequest(anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getApplicantCommonVars(any(), any(CicCase.class))).thenReturn(new HashMap<>());
        doNothing().when(notificationHelper).addAddressTemplateVars(any(AddressGlobalUK.class), anyMap());
        newOrderIssuedNotification.sendToApplicant(data, "CN1");

        //Then
        verify(notificationService).sendLetter(any(NotificationRequest.class));
    }

    private CaseData getMockCaseData() {
        CicCase cicCase = CicCase.builder()
            .fullName("fullName").caseNumber("CN1")
            .build();

        return CaseData.builder().cicCase(cicCase).build();
    }
}
