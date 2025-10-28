package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.caseworker.util.EventConstants;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import static io.restassured.RestAssured.when;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class CaseworkerCreateAndSendOrderTest {

    @InjectMocks
    private CaseworkerCreateAndSendOrder caseworkerCreateAndSendOrder;

    @Test
    void  shouldSetCurrentEventInAboutToStartCallback() {
        final CaseData caseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        final var response = caseworkerCreateAndSendOrder.aboutToStart(details);

        assertThat(response.getData().getCurrentEvent()).isEqualTo(EventConstants.CASEWORKER_CREATE_AND_SEND_ORDER);
    }

    @Test
    void shouldSuccessfullyCreateAndSendNewOrder() {
        // test new order creation and sending logic
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        details.setData(caseData);

//        when()
        final var response = caseworkerCreateAndSendOrder.aboutToSubmit(details, details);

        assertThat(response).isNotNull();
    }
}
