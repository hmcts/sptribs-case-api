package uk.gov.hmcts.sptribs.ciccase;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.CaseViewRequest;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.CriminalInjuriesCompensationData;

import static org.assertj.core.api.Assertions.assertThat;

class CicCaseViewTest {

    private final CicCaseView cicCaseView = new CicCaseView();

    @Test
    void shouldReturnProvidedCaseDataUnchanged() {
        CriminalInjuriesCompensationData caseData = new CriminalInjuriesCompensationData();
        caseData.setCaseNameHmctsInternal("Sample case");
        CaseViewRequest<State> request = new CaseViewRequest<>(1234567890123456L, State.CaseManagement);

        var returnedCaseData = cicCaseView.getCase(request, caseData);

        assertThat(returnedCaseData).isSameAs(caseData);
    }
}
