package uk.gov.hmcts.sptribs.ciccase.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;

class FinalOrderTest {

    @Test
    void shouldReturnDateUntilRespondentCanApplyForFinalOrder() {
        //When
        final FinalOrder finalOrder = FinalOrder.builder()
            .dateFinalOrderEligibleFrom(LocalDate.of(2021, 12, 7))
            .build();
        //Then
        assertThat(finalOrder.calculateDateFinalOrderEligibleToRespondent())
            .isEqualTo(LocalDate.of(2022, 3, 7));
    }

    @Test
    void shouldReturnDateUntilCaseIsNoLongerEligibleForFinalOrder() {
        //When
        final LocalDate coPronouncementDate = LocalDate.of(2021, 12, 7);
        final FinalOrder finalOrder = FinalOrder.builder().build();
        //Then
        assertThat(finalOrder.calculateDateFinalOrderNoLongerEligible(coPronouncementDate))
            .isEqualTo(LocalDate.of(2022, 12, 7));
    }

    @Test
    void shouldReturnDateFinalOrderEligibleFrom() {
        //When
        final LocalDateTime dateTime = LocalDateTime.of(2021, 12, 7, 10, 12, 0);
        final FinalOrder finalOrder = FinalOrder.builder().build();
        //Then
        assertThat(finalOrder.getDateFinalOrderEligibleFrom(dateTime))
            .isEqualTo(LocalDate.of(2022, 1, 19));
    }

    @Test
    void shouldReturnTrueIfFinalOrderHasBeenSentToApplicant1() {
        //When
        final FinalOrder finalOrder = FinalOrder.builder()
            .finalOrderReminderSentApplicant1(YES)
            .build();
        //Then
        assertThat(finalOrder.hasFinalOrderReminderSentApplicant1()).isTrue();
    }
}
