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
class CaseworkerAnonymiseCaseTest {
    @InjectMocks
    private CaseworkerAnonymiseCase caseworkerAnonymiseCase;

    @Test
    void shouldAnonymiseCaseSuccessfully() {
        final CaseData caseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        final var cicCase = caseData.getCicCase();
        cicCase.setAnonymisedAppellantName("Anonymised");
        caseData.setCicCase(cicCase);

        final CaseDetails<CaseData, State> detailsBefore = new CaseDetails<>();
        final CaseData caseDataBefore = CaseData.builder().build();
        detailsBefore.setData(caseDataBefore);

        var response = caseworkerAnonymiseCase.aboutToSubmit(details, detailsBefore);

        assertThat(response).isNotNull();
        assertThat(response.getData().getCicCase().getAnonymisedAppellantName()).isEqualTo("Anonymised");
    }
}
