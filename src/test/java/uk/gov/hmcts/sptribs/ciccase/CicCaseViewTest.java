package uk.gov.hmcts.sptribs.ciccase;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.CaseViewRequest;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import static org.assertj.core.api.Assertions.assertThat;

class CicCaseViewTest {

    private final CicCaseView cicCaseView = new CicCaseView();

    @Test
    void shouldReturnProvidedCaseDataUnchanged() {
        CaseData caseData = CaseData.builder()
            .caseNameHmctsInternal("Sample case")
            .build();
        CaseViewRequest<State> request = new CaseViewRequest<>(1234567890123456L, State.CaseManagement);

        CaseData returnedCaseData = cicCaseView.getCase(request, caseData);

        assertThat(returnedCaseData).isSameAs(caseData);
    }
}
