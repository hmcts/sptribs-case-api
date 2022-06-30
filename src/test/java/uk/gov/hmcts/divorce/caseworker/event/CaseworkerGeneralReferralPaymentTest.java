package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.ciccase.model.CaseData;
import uk.gov.hmcts.divorce.ciccase.model.GeneralReferral;
import uk.gov.hmcts.divorce.ciccase.model.State;
import uk.gov.hmcts.divorce.ciccase.model.UserRole;
import uk.gov.hmcts.divorce.payment.PaymentService;
import uk.gov.hmcts.divorce.testutil.ConfigTestUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerGeneralReferralPayment.CASEWORKER_GENERAL_REFERRAL_PAYMENT;
import static uk.gov.hmcts.divorce.payment.PaymentService.EVENT_GENERAL;
import static uk.gov.hmcts.divorce.payment.PaymentService.KEYWORD_DEEMED;
import static uk.gov.hmcts.divorce.payment.PaymentService.SERVICE_OTHER;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class CaseworkerGeneralReferralPaymentTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private CaseworkerGeneralReferralPayment generalReferralPayment;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = ConfigTestUtil.createCaseDataConfigBuilder();

        generalReferralPayment.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .containsExactly(CASEWORKER_GENERAL_REFERRAL_PAYMENT);
    }

    @Test
    void shouldPopulateGeneralReferralFeeOrderSummaryInAboutToStart() {
        final CaseData caseData = caseData();
        caseData.setGeneralReferral(GeneralReferral.builder().build());
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        final OrderSummary orderSummary = OrderSummary.builder().build();

        when(paymentService.getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_GENERAL, KEYWORD_DEEMED))
            .thenReturn(orderSummary);

        AboutToStartOrSubmitResponse<CaseData, State> response = generalReferralPayment.aboutToStart(details);

        assertThat(response.getData().getGeneralReferral().getGeneralReferralFee().getOrderSummary()).isNotNull();
        assertThat(response.getData().getGeneralReferral().getGeneralReferralFee().getOrderSummary()).isEqualTo(orderSummary);
    }
}
