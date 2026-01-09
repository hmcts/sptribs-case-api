package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.FlagDetail;
import uk.gov.hmcts.ccd.sdk.type.Flags;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.ApplyAnonymity;
import uk.gov.hmcts.sptribs.caseworker.event.page.DraftOrderFooter;
import uk.gov.hmcts.sptribs.caseworker.model.DateModel;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderContentCIC;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.caseworker.model.OrderIssuingType;
import uk.gov.hmcts.sptribs.caseworker.util.CaseFlagsUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.PartiesCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.notification.dispatcher.NewOrderIssuedNotification;
import uk.gov.hmcts.sptribs.notification.exception.NotificationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.sptribs.caseworker.model.OrderIssuingType.CREATE_AND_SEND_NEW_ORDER;
import static uk.gov.hmcts.sptribs.caseworker.model.OrderIssuingType.UPLOAD_A_NEW_ORDER_FROM_YOUR_COMPUTER;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CREATE_AND_SEND_ORDER;
import static uk.gov.hmcts.sptribs.ciccase.model.ApplicantCIC.APPLICANT_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC.REPRESENTATIVE;
import static uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC.RESPONDENT;
import static uk.gov.hmcts.sptribs.ciccase.model.SchemeCic.Year2012;
import static uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC.SUBJECT;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CaseworkerCreateAndSendOrderTest {

    @InjectMocks
    private CaseworkerCreateAndSendOrder caseworkerCreateAndSendOrder;

    @Mock
    private ApplyAnonymity applyAnonymity;

    @Mock
    private DraftOrderFooter draftOrderFooter;

    @Mock
    private NewOrderIssuedNotification newOrderIssuedNotification;

    @Test
    void shouldAddConfigurationToConfigBuilder() {

        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();
        caseworkerCreateAndSendOrder.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_CREATE_AND_SEND_ORDER);
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getName)
            .contains("Orders: Create and send order");
    }

    @Test
    void shouldSetAnonymityAlreadyAppliedInAboutToStartCallback_AnonymityAlreadyAppliedIsNull() {
        final CaseData caseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder()
            .anonymiseYesOrNo(YesOrNo.YES)
            .anonymityAlreadyApplied(null)
            .anonymisedAppellantName("AA")
            .build();
        caseData.setCicCase(cicCase);
        details.setData(caseData);

        final var response = caseworkerCreateAndSendOrder.aboutToStart(details);

        assertThat(response.getData().getCicCase().getAnonymityAlreadyApplied()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldSetAnonymityAlreadyAppliedInAboutToStartCallback_AnonymityAlreadyAppliedIsYes() {
        final CaseData caseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder()
            .anonymiseYesOrNo(YesOrNo.YES)
            .anonymityAlreadyApplied(YesOrNo.YES)
            .anonymisedAppellantName("AA")
            .build();
        caseData.setCicCase(cicCase);
        details.setData(caseData);

        final var response = caseworkerCreateAndSendOrder.aboutToStart(details);

        assertThat(response.getData().getCicCase().getAnonymityAlreadyApplied()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldSetAnonymityAlreadyAppliedInAboutToStartCallback_AnonymityAlreadyAppliedIsNo() {
        final CaseData caseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder()
            .anonymiseYesOrNo(YesOrNo.YES)
            .anonymityAlreadyApplied(YesOrNo.NO)
            .anonymisedAppellantName("AA")
            .build();
        caseData.setCicCase(cicCase);
        details.setData(caseData);

        final var response = caseworkerCreateAndSendOrder.aboutToStart(details);

        assertThat(response.getData().getCicCase().getAnonymityAlreadyApplied()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldSetAnonymityAlreadyAppliedInAboutToStartCallback_NoAnonymitySet() {
        final CaseData caseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder()
            .anonymiseYesOrNo(null)
            .anonymityAlreadyApplied(YesOrNo.NO)
            .anonymisedAppellantName(null)
            .build();
        caseData.setCicCase(cicCase);
        details.setData(caseData);

        final var response = caseworkerCreateAndSendOrder.aboutToStart(details);

        assertThat(response.getData().getCicCase().getAnonymityAlreadyApplied()).isEqualTo(YesOrNo.NO);
    }

    @Test
    void shouldSuccessfullyCreateAndSendNewAnonymisedOrder() {
        DraftOrderContentCIC draftOrderContentCIC = DraftOrderContentCIC.builder()
                .orderTemplate(OrderTemplate.CIC3_RULE_27)
                .mainContent("Main order content sample")
                .orderSignature("Supreme Judge Fudge")
                .build();

        Document document = Document.builder()
                .categoryId("TD")
                .filename("Order--[AAC]--09-05-2024 09:04:04.pdf")
                .binaryUrl("url/documents/uuid/binary")
                .url("url/documents/uuid")
                .build();

        final CaseData caseData = CaseData.builder()
                .draftOrderContentCIC(draftOrderContentCIC)
                .cicCase(getCicCase(CREATE_AND_SEND_NEW_ORDER, YesOrNo.YES, "AAC", document))
                .build();

        caseData.setDraftOrderContentCIC(draftOrderContentCIC);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        final var response = caseworkerCreateAndSendOrder.aboutToSubmit(details, caseDetailsBefore());

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotNull();

        final CicCase cicCase = response.getData().getCicCase();
        assertThat(cicCase.getAnonymisedAppellantName()).isEqualTo("AAC");
        assertThat(cicCase.getAnonymiseYesOrNo()).isEqualTo(YesOrNo.YES);

        List<ListValue<Order>> orderList = cicCase.getOrderList();
        assertThat(orderList).isNotNull().hasSize(1);

        final DraftOrderCIC draftOrderCIC = DraftOrderCIC.builder()
                .draftOrderContentCIC(draftOrderContentCIC)
                .templateGeneratedDocument(document)
                .build();
        final Order expectedOrder = getExpectedOrder(draftOrderCIC, null);
        Order order = orderList.getFirst().getValue();
        assertThat(order).isEqualTo(expectedOrder);

        final var submittedResponse = caseworkerCreateAndSendOrder.submitted(details, caseDetailsBefore());
        assertThat(submittedResponse.getConfirmationHeader()).contains("# Order sent");
    }

    @Test
    void shouldSuccessfullyCreateAndSendNewNonAnonymisedOrder() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();

        Document document = Document.builder()
                .categoryId("TD")
                .filename("Order--[Test Name]--09-05-2024 09:04:04.pdf")
                .binaryUrl("url/documents/uuid/binary")
                .url("url/documents/uuid")
                .build();

        DraftOrderContentCIC draftOrderContentCIC = DraftOrderContentCIC.builder()
                .orderTemplate(OrderTemplate.CIC3_RULE_27)
                .mainContent("Main order content sample")
                .orderSignature("Supreme Judge Fudge")
                .build();

        final CaseData caseData = CaseData.builder().build();
        caseData.setCicCase(getCicCase(CREATE_AND_SEND_NEW_ORDER, YesOrNo.NO, null, document));
        caseData.setDraftOrderContentCIC(draftOrderContentCIC);

        details.setData(caseData);

        final var response = caseworkerCreateAndSendOrder.aboutToSubmit(details, caseDetailsBefore());

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotNull();

        final CicCase cicCase = response.getData().getCicCase();
        assertThat(cicCase.getAnonymiseYesOrNo()).isEqualTo(YesOrNo.NO);
        assertThat(cicCase.getAnonymisedAppellantName()).isNull();

        List<ListValue<Order>> orderList = cicCase.getOrderList();
        assertThat(orderList).isNotNull().hasSize(1);
        final  DraftOrderCIC draftOrderCIC = DraftOrderCIC.builder()
                .draftOrderContentCIC(draftOrderContentCIC)
                .templateGeneratedDocument(document)
                .build();
        final Order expectedOrder = getExpectedOrder(draftOrderCIC, null);
        Order order = orderList.getFirst().getValue();
        assertThat(order).isEqualTo(expectedOrder);

        final var submittedResponse = caseworkerCreateAndSendOrder.submitted(details, caseDetailsBefore());
        assertThat(submittedResponse.getConfirmationHeader()).contains("# Order sent");
    }

    @Test
    void shouldApplyAnonymisationCaseFlagWhenAnonymityIsApplied() {
        DraftOrderContentCIC draftOrderContentCIC = DraftOrderContentCIC.builder()
                .orderTemplate(OrderTemplate.CIC3_RULE_27)
                .mainContent("Main order content sample")
                .orderSignature("Supreme Judge Fudge")
                .build();

        Document document = Document.builder()
                .categoryId("TD")
                .filename("Order--[AAC]--09-05-2024 09:04:04.pdf")
                .binaryUrl("url/documents/uuid/binary")
                .url("url/documents/uuid")
                .build();

        final CaseData caseData = CaseData.builder()
                .draftOrderContentCIC(draftOrderContentCIC)
                .cicCase(getCicCase(CreateAndSendIssuingType.CREATE_AND_SEND_NEW_ORDER, YesOrNo.YES, "AAC", document))
                .build();

        caseData.setDraftOrderContentCIC(draftOrderContentCIC);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        final var response = caseworkerCreateAndSendOrder.aboutToSubmit(details, caseDetailsBefore());

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getCaseFlags()).isNotNull();
        assertThat(response.getData().getCaseFlags().getDetails()).hasSize(1);
        assertThat(response.getData().getCaseFlags().getDetails().getFirst().getValue())
            .hasFieldOrPropertyWithValue("name", getExpectedAnonymisationFlag().getName())
            .hasFieldOrPropertyWithValue("flagCode", getExpectedAnonymisationFlag().getFlagCode())
            .hasFieldOrPropertyWithValue("status", getExpectedAnonymisationFlag().getStatus());


        final CicCase cicCase = response.getData().getCicCase();
        assertThat(cicCase.getAnonymisedAppellantName()).isEqualTo("AAC");
        assertThat(cicCase.getAnonymiseYesOrNo()).isEqualTo(YesOrNo.YES);

        List<ListValue<Order>> orderList = cicCase.getOrderList();
        assertThat(orderList).isNotNull().hasSize(1);

        final DraftOrderCIC draftOrderCIC = DraftOrderCIC.builder()
                .draftOrderContentCIC(draftOrderContentCIC)
                .templateGeneratedDocument(document)
                .build();
        final Order expectedOrder = getExpectedOrder(draftOrderCIC, null);
        Order order = orderList.getFirst().getValue();
        assertThat(order).isEqualTo(expectedOrder);

        final var submittedResponse = caseworkerCreateAndSendOrder.submitted(details,caseDetailsBefore());
        assertThat(submittedResponse.getConfirmationHeader()).contains("# Order sent");
    }

    @Test
    void shouldNotApplyAnonymisationCaseFlagWhenAnonymityFlagExists() {
        DraftOrderContentCIC draftOrderContentCIC = DraftOrderContentCIC.builder()
                .orderTemplate(OrderTemplate.CIC3_RULE_27)
                .mainContent("Main order content sample")
                .orderSignature("Supreme Judge Fudge")
                .build();

        Document document = Document.builder()
                .categoryId("TD")
                .filename("Order--[AAC]--09-05-2024 09:04:04.pdf")
                .binaryUrl("url/documents/uuid/binary")
                .url("url/documents/uuid")
                .build();

        List<ListValue<FlagDetail>> flagDetailsList = new ArrayList<>();
        flagDetailsList.add(ListValue.<FlagDetail>builder().value(getExpectedAnonymisationFlag()).build());
        Flags flags = Flags.builder()
                .details(flagDetailsList)
                .build();


        final CaseData caseData = CaseData.builder()
                .caseFlags(flags)
                .draftOrderContentCIC(draftOrderContentCIC)
                .cicCase(getCicCase(CreateAndSendIssuingType.CREATE_AND_SEND_NEW_ORDER, YesOrNo.YES, "AAC", document))
                .build();

        caseData.setDraftOrderContentCIC(draftOrderContentCIC);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        final var response = caseworkerCreateAndSendOrder.aboutToSubmit(details, caseDetailsBefore());

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getCaseFlags()).isNotNull();
        assertThat(response.getData().getCaseFlags().getDetails()).hasSize(1);
        assertTrue(CaseFlagsUtil.caseFlagDetailsEquals(
                response.getData().getCaseFlags().getDetails().getFirst().getValue(), getExpectedAnonymisationFlag()));

        final CicCase cicCase = response.getData().getCicCase();
        assertThat(cicCase.getAnonymisedAppellantName()).isEqualTo("AAC");
        assertThat(cicCase.getAnonymiseYesOrNo()).isEqualTo(YesOrNo.YES);

        List<ListValue<Order>> orderList = cicCase.getOrderList();
        assertThat(orderList).isNotNull().hasSize(1);

        final DraftOrderCIC draftOrderCIC = DraftOrderCIC.builder()
                .draftOrderContentCIC(draftOrderContentCIC)
                .templateGeneratedDocument(document)
                .build();
        final Order expectedOrder = getExpectedOrder(draftOrderCIC, null);
        Order order = orderList.getFirst().getValue();
        assertThat(order).isEqualTo(expectedOrder);

        final var submittedResponse = caseworkerCreateAndSendOrder.submitted(details,caseDetailsBefore());
        assertThat(submittedResponse.getConfirmationHeader()).contains("# Order sent");
    }

    @Test
    void shouldSuccessfullySendUploadedOrder() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();

        Document document = Document.builder()
            .filename("Order--[Test Name]--09-05-2024 09:04:04.pdf")
            .binaryUrl("url/documents/uuid/binary")
            .url("url/documents/uuid")
            .build();

        CICDocument cicDocument = CICDocument.builder()
            .documentLink(document)
            .documentEmailContent("Some test content")
            .build();

        final CaseData caseData = CaseData.builder().build();
        CicCase cicCase1 = getCicCase(UPLOAD_A_NEW_ORDER_FROM_YOUR_COMPUTER, YesOrNo.NO, null, null);
        List<ListValue<CICDocument>> orderFile = List.of(ListValue.<CICDocument>builder().value(cicDocument).build());
        cicCase1.setOrderFile(orderFile);
        caseData.setCicCase(cicCase1);

        details.setData(caseData);

        final var response = caseworkerCreateAndSendOrder.aboutToSubmit(details, caseDetailsBefore());

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotNull();

        final CicCase cicCase = response.getData().getCicCase();
        assertThat(cicCase.getAnonymiseYesOrNo()).isEqualTo(YesOrNo.NO);
        assertThat(cicCase.getAnonymisedAppellantName()).isNull();

        List<ListValue<Order>> orderList = cicCase.getOrderList();
        assertThat(orderList).isNotNull().hasSize(1);

        final Order expectedOrder = getExpectedOrder(null, orderFile);
        Order order = orderList.getFirst().getValue();
        assertThat(order).isEqualTo(expectedOrder);
        assertThat(order.getUploadedFile().getFirst().getValue().getDocumentLink().getCategoryId()).isEqualTo("TD");

        final var submittedResponse = caseworkerCreateAndSendOrder.submitted(details, caseDetailsBefore());
        assertThat(submittedResponse.getConfirmationHeader()).contains("# Order sent");
    }

    @Test
    void shouldShowErrorMessageWhenNotificationFailsForSubject() {
        final CaseData caseData = caseData();
        final String hyphenatedCaseRef = caseData.formatCaseRef(TEST_CASE_ID);
        caseData.setHyphenatedCaseRef(hyphenatedCaseRef);
        caseData.getCicCase().setNotifyPartySubject(Set.of(SUBJECT));
        caseData.getCicCase().setNotifyPartyApplicant(Set.of(APPLICANT_CIC));
        caseData.getCicCase().setNotifyPartyRepresentative(Set.of(REPRESENTATIVE));
        caseData.getCicCase().setNotifyPartyRespondent(Set.of(RESPONDENT));

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        doThrow(NotificationException.class)
                .when(newOrderIssuedNotification)
                .sendToSubject(caseData, hyphenatedCaseRef);

        SubmittedCallbackResponse submittedResponse = caseworkerCreateAndSendOrder.submitted(caseDetails, caseDetailsBefore());

        assertThat(submittedResponse.getConfirmationHeader())
                .isEqualTo("""
                    # Send order notification failed\s
                    ## Please resend the order""");
        verify(newOrderIssuedNotification, times(1)).sendToSubject(any(CaseData.class), anyString());

        verify(newOrderIssuedNotification, never()).sendToRepresentative(any(CaseData.class), anyString());
        verify(newOrderIssuedNotification, never()).sendToRespondent(any(CaseData.class), anyString());
        verify(newOrderIssuedNotification, never()).sendToApplicant(any(CaseData.class), anyString());
    }

    @Test
    void shouldShowErrorMessageWhenNotificationFailsForRepresentative() {
        final CaseData caseData = caseData();
        final String hyphenatedCaseRef = caseData.formatCaseRef(TEST_CASE_ID);
        caseData.setHyphenatedCaseRef(hyphenatedCaseRef);
        caseData.getCicCase().setNotifyPartySubject(Set.of(SUBJECT));
        caseData.getCicCase().setNotifyPartyApplicant(Set.of(APPLICANT_CIC));
        caseData.getCicCase().setNotifyPartyRepresentative(Set.of(REPRESENTATIVE));
        caseData.getCicCase().setNotifyPartyRespondent(Set.of(RESPONDENT));

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        doThrow(NotificationException.class)
                .when(newOrderIssuedNotification)
                .sendToRepresentative(caseData, hyphenatedCaseRef);

        SubmittedCallbackResponse submittedResponse = caseworkerCreateAndSendOrder.submitted(caseDetails, caseDetailsBefore());

        assertThat(submittedResponse.getConfirmationHeader())
                .isEqualTo("""
                    # Send order notification failed\s
                    ## Please resend the order""");
        verify(newOrderIssuedNotification, times(1)).sendToSubject(any(CaseData.class), anyString());
        verify(newOrderIssuedNotification, times(1)).sendToRepresentative(any(CaseData.class), anyString());

        verify(newOrderIssuedNotification, never()).sendToRespondent(any(CaseData.class), anyString());
        verify(newOrderIssuedNotification, never()).sendToApplicant(any(CaseData.class), anyString());

    }

    @Test
    void shouldShowErrorMessageWhenNotificationFailsForRespondent() {
        final CaseData caseData = caseData();
        final String hyphenatedCaseRef = caseData.formatCaseRef(TEST_CASE_ID);
        caseData.setHyphenatedCaseRef(hyphenatedCaseRef);
        caseData.getCicCase().setNotifyPartySubject(Set.of(SUBJECT));
        caseData.getCicCase().setNotifyPartyApplicant(Set.of(APPLICANT_CIC));
        caseData.getCicCase().setNotifyPartyRepresentative(Set.of(REPRESENTATIVE));
        caseData.getCicCase().setNotifyPartyRespondent(Set.of(RESPONDENT));

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        doThrow(NotificationException.class)
                .when(newOrderIssuedNotification)
                .sendToRespondent(caseData, hyphenatedCaseRef);

        SubmittedCallbackResponse submittedResponse = caseworkerCreateAndSendOrder.submitted(caseDetails, caseDetailsBefore());

        assertThat(submittedResponse.getConfirmationHeader())
                .isEqualTo("""
                    # Send order notification failed\s
                    ## Please resend the order""");
        verify(newOrderIssuedNotification, times(1)).sendToSubject(any(CaseData.class), anyString());
        verify(newOrderIssuedNotification, times(1)).sendToRepresentative(any(CaseData.class), anyString());
        verify(newOrderIssuedNotification, times(1)).sendToRespondent(any(CaseData.class), anyString());

        verify(newOrderIssuedNotification, never()).sendToApplicant(any(CaseData.class), anyString());
    }

    @Test
    void shouldShowErrorMessageWhenNotificationFailsForApplicant() {
        final CaseData caseData = caseData();
        final String hyphenatedCaseRef = caseData.formatCaseRef(TEST_CASE_ID);
        caseData.setHyphenatedCaseRef(hyphenatedCaseRef);
        caseData.getCicCase().setNotifyPartySubject(Set.of(SUBJECT));
        caseData.getCicCase().setNotifyPartyApplicant(Set.of(APPLICANT_CIC));
        caseData.getCicCase().setNotifyPartyRepresentative(Set.of(REPRESENTATIVE));
        caseData.getCicCase().setNotifyPartyRespondent(Set.of(RESPONDENT));

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        doThrow(NotificationException.class)
                .when(newOrderIssuedNotification)
                .sendToApplicant(caseData, hyphenatedCaseRef);

        SubmittedCallbackResponse submittedResponse = caseworkerCreateAndSendOrder.submitted(caseDetails, caseDetailsBefore());

        assertThat(submittedResponse.getConfirmationHeader())
                .isEqualTo("""
                    # Send order notification failed\s
                    ## Please resend the order""");
        verify(newOrderIssuedNotification, times(1)).sendToSubject(any(CaseData.class), anyString());
        verify(newOrderIssuedNotification, times(1)).sendToRepresentative(any(CaseData.class), anyString());
        verify(newOrderIssuedNotification, times(1)).sendToRespondent(any(CaseData.class), anyString());
        verify(newOrderIssuedNotification, times(1)).sendToApplicant(any(CaseData.class), anyString());
    }

    private CaseDetails<CaseData, State> caseDetailsBefore() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseDetails.setData(caseData);
        return caseDetails;
    }

    private static CicCase getCicCase(OrderIssuingType issueType,
                                      YesOrNo isAnonymised,
                                      String anonymisedName,
                                      Document document) {
        DateModel dateModel = DateModel.builder()
                .dueDate(LocalDate.of(2026, 1, 2))
                .information("due date for test")
                .build();

        return CicCase.builder()
            .orderIssuingType(issueType)
            .anonymiseYesOrNo(isAnonymised)
            .anonymisedAppellantName(anonymisedName)
            .orderTemplateIssued(document)
            .partiesCIC(Set.of(PartiesCIC.SUBJECT, PartiesCIC.REPRESENTATIVE))
            .notifyPartySubject(Set.of(SUBJECT))
            .notifyPartyRespondent(Set.of(RESPONDENT))
            .notifyPartyRepresentative(Set.of(REPRESENTATIVE))
            .notifyPartyApplicant(Set.of(APPLICANT_CIC))
            .orderDueDates(List.of(ListValue.<DateModel>builder().value(dateModel).build()))
            .fullName("Test Name")
            .schemeCic(Year2012)
            .build();
    }

    private Order getExpectedOrder(DraftOrderCIC draftOrderCIC, List<ListValue<CICDocument>> uploadedFile) {
        return Order.builder()
            .uploadedFile(uploadedFile)
            .draftOrder(draftOrderCIC)
            .parties("Subject, Respondent, Representative, Applicant")
            .dueDateList(List.of(
                ListValue.<DateModel>builder()
                    .value(DateModel.builder()
                        .dueDate(LocalDate.of(2026, 1, 2))
                        .information("due date for test")
                        .build())
                    .build()))
            .orderSentDate(LocalDate.now())
            .build();
    }

    private FlagDetail getExpectedAnonymisationFlag() {
        return FlagDetail.builder()
            .name("RRO (Restricted Reporting Order / Anonymisation)")
            .path(List.of(ListValue.<String>builder().id(UUID.randomUUID().toString()).value("Case").build()))
            .status("Active")
            .nameCy("RRO (Gorchymyn Cyfyngiadau Adrodd / Anhysbys)")
            .flagCode("CF0012")
            .flagComment("Applied anonymity")
            .dateTimeCreated(LocalDateTime.now())
            .hearingRelevant(YesOrNo.YES)
            .availableExternally(YesOrNo.NO)
            .build();
    }

}
