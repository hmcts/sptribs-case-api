package uk.gov.hmcts.sptribs.ciccase;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.CaseViewRequest;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.repositories.CorrespondenceRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CicCaseViewTest {

    private final CorrespondenceRepository correspondenceRepository = mock(CorrespondenceRepository.class);
    private final CicCaseView cicCaseView = new CicCaseView(correspondenceRepository);

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
