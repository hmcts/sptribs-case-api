package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.caseworker.event.page.DraftOrderFooter;
import uk.gov.hmcts.sptribs.caseworker.model.CreateAndSendIssueType;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderContentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.notification.dispatcher.NewOrderIssuedNotification;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CREATE_AND_SEND_ORDER;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class CaseworkerCreateAndSendOrderTest {

    @InjectMocks
    private CaseworkerCreateAndSendOrder caseworkerCreateAndSendOrder;

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
    void  shouldSetCurrentEventInAboutToStartCallback() {
        final CaseData caseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        final var response = caseworkerCreateAndSendOrder.aboutToStart(details);

        assertThat(response.getData().getCurrentEvent()).isEqualTo(CASEWORKER_CREATE_AND_SEND_ORDER);
    }

    @Test
    void shouldSuccessfullyCreateAndSendNewAnonymisedOrder() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        details.setData(caseData);
        final CicCase cicCase = CicCase.builder()
            .anonymiseYesOrNo(YesOrNo.YES)
            .anonymisedAppellantName("Anonymised Name")
            .createAndSendIssuingTypes(CreateAndSendIssueType.CREATE_AND_SEND_NEW_ORDER)
            .build();
        caseData.setCicCase(cicCase);

        DraftOrderContentCIC draftOrderContentCIC = DraftOrderContentCIC.builder()
            .orderTemplate(OrderTemplate.CIC3_RULE_27)
            .mainContent("Sample main content")
            .orderSignature("Test Judge")
            .build();

        caseData.setDraftOrderContentCIC(draftOrderContentCIC);

        final var response = caseworkerCreateAndSendOrder.aboutToSubmit(details, details);

        assertThat(response).isNotNull();

        final var submittedResponse = caseworkerCreateAndSendOrder.submitted(details, details);
        assertThat(submittedResponse.getConfirmationHeader()).contains("# Order sent");
    }

    @Test
    void shouldSuccessfullyCreateAndSendNewNonAnonymisedOrder() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        details.setData(caseData);
        final CicCase cicCase = CicCase.builder()
                .anonymiseYesOrNo(YesOrNo.NO)
                .createAndSendIssuingTypes(CreateAndSendIssueType.CREATE_AND_SEND_NEW_ORDER)
                .build();
        caseData.setCicCase(cicCase);

        DraftOrderContentCIC draftOrderContentCIC = DraftOrderContentCIC.builder()
                .orderTemplate(OrderTemplate.CIC3_RULE_27)
                .mainContent("Sample main content")
                .orderSignature("Test Judge")
                .build();
        caseData.setDraftOrderContentCIC(draftOrderContentCIC);

        final var response = caseworkerCreateAndSendOrder.aboutToSubmit(details, caseDetailsBefore());

        assertThat(response).isNotNull();
    }

    @Test
    void shouldSuccessfullySendUploadedOrder() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        details.setData(caseData);
        final CicCase cicCase = CicCase.builder()
            .createAndSendIssuingTypes(CreateAndSendIssueType.UPLOAD_A_NEW_ORDER_FROM_YOUR_COMPUTER)
            .build();
        caseData.setCicCase(cicCase);

        final var response = caseworkerCreateAndSendOrder.aboutToSubmit(details, caseDetailsBefore());

        assertThat(response).isNotNull();

        final var submittedResponse = caseworkerCreateAndSendOrder.submitted(details, details);
        assertThat(submittedResponse.getConfirmationHeader()).contains("# Order sent");
    }

    @Test
    void shouldShowErrorMessageWhenUploadFails() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        details.setData(caseData);
        final CicCase cicCase = CicCase.builder()
            .createAndSendIssuingTypes(CreateAndSendIssueType.UPLOAD_A_NEW_ORDER_FROM_YOUR_COMPUTER)
            .build();
        caseData.setCicCase(cicCase);

        DraftOrderContentCIC draftOrderContentCIC = DraftOrderContentCIC.builder()
            .orderTemplate(OrderTemplate.CIC3_RULE_27)
            .mainContent("Sample main content")
            .orderSignature("Test Judge")
            .build();
        caseData.setDraftOrderContentCIC(draftOrderContentCIC);

        final var response = caseworkerCreateAndSendOrder.aboutToSubmit(details, details);

        assertThat(response).isNotNull();
    }

    private CaseDetails<CaseData, State> caseDetailsBefore() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseDetails.setData(caseData);
        return caseDetails;
    }
}
