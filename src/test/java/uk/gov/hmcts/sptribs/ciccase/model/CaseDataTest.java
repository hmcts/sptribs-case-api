package uk.gov.hmcts.sptribs.ciccase.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CaseDataTest {

    @Test
    void shouldReturnNullIfApplicant2EmailIsNullAndCaseInviteIsNull() {
        //When
        final CaseData caseData = CaseData.builder()
            .build();
        //Then
        assertThat(caseData.getCicCase().getApplicantFullName()).isNull();
    }
}
