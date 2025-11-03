package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CaseworkerAnonymizeCaseTest {
    @InjectMocks
    private CaseworkerAnonymizeCase caseworkerAnonymizeCase;

    @Test
    void shouldAnonymizeCaseSuccessfully() {
        final CaseData caseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        var response = caseworkerAnonymizeCase.aboutToSubmit(details, details);

        assertThat(response).isNotNull();
    }
}
