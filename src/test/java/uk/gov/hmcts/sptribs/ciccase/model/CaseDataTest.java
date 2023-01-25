package uk.gov.hmcts.sptribs.ciccase.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.ciccase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.sptribs.ciccase.model.DivorceOrDissolution.DIVORCE;

class CaseDataTest {

    @Test
    void shouldReturnTrueIfCaseDataIsDivorce() {

        //When
        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DIVORCE)
            .build();
        //Then
        assertThat(caseData.isDivorce()).isTrue();
    }

    @Test
    void shouldReturnFalseIfCaseDataIsDissolution() {
        //When
        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DISSOLUTION)
            .build();
        //Then
        assertThat(caseData.isDivorce()).isFalse();
    }
}
