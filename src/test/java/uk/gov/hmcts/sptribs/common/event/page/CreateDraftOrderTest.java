package uk.gov.hmcts.sptribs.common.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderContentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class CreateDraftOrderTest {

    @InjectMocks
    private CreateDraftOrder createDraftOrder;

    @Test
    void shouldSetDraftOrderContentFromSelectedOrder() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        DraftOrderContentCIC contentCIC = DraftOrderContentCIC.builder()
            .orderTemplate(OrderTemplate.CIC7_ME_DMI_REPORTS)
            .build();

        final CicCase cicCase = CicCase.builder()
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .draftOrderContentCIC(contentCIC)
            .build();
        caseDetails.setData(caseData);

        createDraftOrder.midEvent(caseDetails, caseDetails);

        assertThat(caseData.getDraftOrderContentCIC().getMainContent()).contains("(Directions for DMI/ Psychological/Psychiatric Report "
            + "– please specify which expert)");
    }

    @Test
    void shouldNotSetReferralTypeForWaIfAlreadyPopulated() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        DraftOrderContentCIC contentCIC = DraftOrderContentCIC.builder()
            .orderTemplate(OrderTemplate.CIC7_ME_DMI_REPORTS)
            .build();

        final CicCase cicCase = CicCase.builder()
            .referralTypeForWA("Set aside request")
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .draftOrderContentCIC(contentCIC)
            .build();
        caseDetails.setData(caseData);

        createDraftOrder.midEvent(caseDetails, caseDetails);

        assertEquals("Set aside request", caseDetails.getData().getCicCase().getReferralTypeForWA());
    }

    @Test
    void shouldSetReferralTypeForWa() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        DraftOrderContentCIC contentCIC = DraftOrderContentCIC.builder()
            .orderTemplate(OrderTemplate.CIC7_ME_DMI_REPORTS)
            .build();

        final CicCase cicCase = CicCase.builder()
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .draftOrderContentCIC(contentCIC)
            .build();
        caseDetails.setData(caseData);

        createDraftOrder.midEvent(caseDetails, caseDetails);

        assertEquals("", caseDetails.getData().getCicCase().getReferralTypeForWA());
    }
}
