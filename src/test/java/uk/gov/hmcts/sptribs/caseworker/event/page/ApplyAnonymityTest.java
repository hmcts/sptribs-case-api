package uk.gov.hmcts.sptribs.caseworker.event.page;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ApplyAnonymityTest {

    @InjectMocks
    private ApplyAnonymity applyAnonymity;

    @Test
    void shouldSuccessfullyApplyAnonymity() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder()
            .anonymiseYesOrNo(YesOrNo.YES)
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);

        var response = applyAnonymity.midEvent(caseDetails, caseDetails);
        assertThat(response).isNotNull();
    }

    @Test
    void shouldNotApplyAnonymityWhenNoSelected() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder()
            .anonymiseYesOrNo(YesOrNo.NO)
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);

        var response = applyAnonymity.midEvent(caseDetails, caseDetails);
        assertThat(response).isNotNull();
    }
}
