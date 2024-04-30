package uk.gov.hmcts.sptribs.common.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.PartiesCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class SelectPartiesTest {

    @InjectMocks
    private SelectParties selectParties;

    @Test
    void shouldSelectSubject() {
        //Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder().partiesCIC(Set.of(PartiesCIC.APPLICANT)).build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);

        //When
        final AboutToStartOrSubmitResponse<CaseData, State> response = selectParties.midEvent(caseDetails, caseDetails);

        //Then
        assertThat(response.getErrors()).isNotNull();
    }
}
