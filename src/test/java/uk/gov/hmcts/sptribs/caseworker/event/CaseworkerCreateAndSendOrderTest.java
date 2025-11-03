package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.caseworker.model.CreateAndSendIssueType;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderContentCIC;
import uk.gov.hmcts.sptribs.caseworker.util.EventConstants;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.State;

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
    void shouldSuccessfullyCreateAndSendNewAnonymisedOrder() {
        // test new order creation and sending logic
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
    }

    @Test
    void shouldSuccessfullyCreateAndSendNewNonAnonymisedOrder() {
        // test new order creation and sending logic
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

        final var response = caseworkerCreateAndSendOrder.aboutToSubmit(details, details);

        assertThat(response).isNotNull();
    }


}
