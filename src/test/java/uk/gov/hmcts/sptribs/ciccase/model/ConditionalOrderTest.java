package uk.gov.hmcts.sptribs.ciccase.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;

class ConditionalOrderTest {

    @Test
    void shouldReturnTrueIf() {
        //When
        final ConditionalOrder conditionalOrder = ConditionalOrder.builder()
            .claimsGranted(YES)
            .build();
        //Then
        assertThat(conditionalOrder.areClaimsGranted()).isTrue();
    }

    @Test
    void shouldReturnFalseIfNotAppliedForFinancialOrder() {
        //When
        final ConditionalOrder conditionalOrder = ConditionalOrder.builder()
            .claimsGranted(NO)
            .build();
        //Then
        assertThat(conditionalOrder.areClaimsGranted()).isFalse();
    }

    @Test
    void shouldReturnFalseIfAppliedForFinancialOrderIsSetToNull() {
        //When
        final ConditionalOrder conditionalOrder = ConditionalOrder.builder()
            .build();
        //Then
        assertThat(conditionalOrder.areClaimsGranted()).isFalse();
    }

    @Test
    void shouldReturnTrueIfSubmittedDateIsNotSet() {
        //When
        final ConditionalOrder conditionalOrder = ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                .build())
            .build();
        //Then
        assertThat(conditionalOrder.isConditionalOrderPending()).isTrue();
    }

    @Test
    void shouldReturnFalseIfSubmittedDateIsSet() {
        //When
        final ConditionalOrder conditionalOrder = ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                .submittedDate(LocalDateTime.now())
                .build())
            .build();
        //Then
        assertThat(conditionalOrder.isConditionalOrderPending()).isFalse();
    }
}
