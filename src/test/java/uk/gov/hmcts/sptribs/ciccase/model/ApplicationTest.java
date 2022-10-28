package uk.gov.hmcts.sptribs.ciccase.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.sptribs.payment.model.Payment;
import uk.gov.hmcts.sptribs.payment.model.PaymentStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.sptribs.ciccase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.sptribs.ciccase.model.ServiceMethod.PERSONAL_SERVICE;
import static uk.gov.hmcts.sptribs.ciccase.model.ServiceMethod.SOLICITOR_SERVICE;
import static uk.gov.hmcts.sptribs.ciccase.model.SolicitorPaymentMethod.FEES_HELP_WITH;
import static uk.gov.hmcts.sptribs.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.sptribs.payment.model.PaymentStatus.DECLINED;
import static uk.gov.hmcts.sptribs.payment.model.PaymentStatus.SUCCESS;

class ApplicationTest {

    @Test
    void shouldReturnTrueIfApplicationHasBeenPaidFor() {
        //Given
        final List<ListValue<Payment>> payments = new ArrayList<>();
        payments.add(paymentValue(payment(55000, SUCCESS)));

        //When
        final var application = Application.builder()
            .applicationFeeOrderSummary(OrderSummary.builder().paymentTotal("55000").build())
            .applicationPayments(payments)
            .build();

        //Then
        assertThat(application.hasBeenPaidFor()).isTrue();
    }

    @Test
    void shouldReturnFalseIfApplicationHasNotBeenPaidFor() {
        //Given
        final List<ListValue<Payment>> payments = new ArrayList<>();
        payments.add(paymentValue(payment(55000, SUCCESS)));

        final var applicationNullOrderSummary = Application.builder()
            .applicationPayments(payments)
            .build();

        //When
        final var applicationOrderSummary = Application.builder()
            .applicationFeeOrderSummary(OrderSummary.builder().paymentTotal("200").build())
            .applicationPayments(payments)
            .build();

        //Then
        assertThat(applicationNullOrderSummary.hasBeenPaidFor()).isFalse();
        assertThat(applicationOrderSummary.hasBeenPaidFor()).isFalse();
    }

    @Test
    void shouldReturnZeroPaymentTotalForNullApplicationPayments() {
        assertThat(Application.builder().build().getPaymentTotal()).isZero();
    }

    @Test
    void shouldReturnSuccessfulPaymentTotalForApplicationPayments() {
        //Given
        final List<ListValue<Payment>> payments = new ArrayList<>();
        payments.add(paymentValue(payment(500, SUCCESS)));
        payments.add(paymentValue(payment(50, SUCCESS)));
        payments.add(paymentValue(payment(50, DECLINED)));

        //When
        final var application = Application.builder()
            .applicationPayments(payments)
            .build();

        //Then
        assertThat(application.getPaymentTotal()).isEqualTo(550);
    }

    @Test
    void shouldReturnLastPaymentStatusAndNullIfEmptyOrNull() {
        //Given
        final List<ListValue<Payment>> payments = new ArrayList<>();
        payments.add(paymentValue(payment(500, SUCCESS)));
        payments.add(paymentValue(payment(50, SUCCESS)));
        payments.add(paymentValue(payment(50, DECLINED)));

        //When
        final var applicationMultiple = Application.builder()
            .applicationPayments(payments)
            .build();
        final var applicationEmpty = Application.builder()
            .applicationPayments(emptyList())
            .build();
        final var applicationNull = Application.builder()
            .build();

        //Then
        assertThat(applicationMultiple.getLastPaymentStatus()).isEqualTo(DECLINED);
        assertThat(applicationEmpty.getLastPaymentStatus()).isNull();
        assertThat(applicationNull.getLastPaymentStatus()).isNull();
    }

    @Test
    void shouldReturnTrueIfStatementOfTruthIsYesForApplicant1() {
        //When
        final var application = Application.builder()
            .applicant1StatementOfTruth(YES)
            .build();

        //Then
        assertThat(application.applicant1HasStatementOfTruth()).isTrue();
    }

    @Test
    void shouldReturnTruePaperCasePaymentMethodSet() {
        //When
        final var application = Application.builder()
            .paperCasePaymentMethod(PaperCasePaymentMethod.CHEQUE_OR_POSTAL_ORDER)
            .build();

        //Then
        assertThat(application.getPaperCasePaymentMethod().getLabel()).isEqualTo("Cheque/Postal Order");
    }

    @Test
    void shouldReturnTrueReissueOptionSet() {
        //When
        final var application = Application.builder()
            .reissueOption(ReissueOption.REISSUE_CASE)
            .build();

        //Then
        assertThat(application.getReissueOption().getLabel()).isEqualTo("Reissue Case");
    }

    @Test
    void shouldReturnTrueProgressPaperCaseSet() {
        //When
        final var application = Application.builder()
            .progressPaperCase(ProgressPaperCase.AWAITING_DOCUMENTS)
            .build();

        //Then
        assertThat(application.getProgressPaperCase().getLabel()).isEqualTo("Awaiting applicant");
    }

    @Test
    void shouldReturnTrueRejectReasonSet() {
        //When
        final RejectReason rejectReason = new RejectReason();
        rejectReason.setRejectReasonType(RejectReasonType.INCORRECT_INFO);
        rejectReason.setRejectDetails("some detail");
        final var application = Application.builder()
            .rejectReason(rejectReason)
            .build();

        //Then
        assertThat(application.getRejectReason().getRejectDetails()).isNotNull();
        assertThat(application.getRejectReason().getRejectReasonType().getLabel()).isEqualTo("Incorrect information");
    }

    @Test
    void shouldReturnFalseIfStatementOfTruthIsNoForApplicant1() {
        //When
        final var application = Application.builder()
            .applicant1StatementOfTruth(NO)
            .build();

        //Then
        assertThat(application.applicant1HasStatementOfTruth()).isFalse();
    }

    @Test
    void shouldReturnTrueIfStatementOfTruthIsYesForSolicitor() {
        //When
        final var application = Application.builder()
            .solSignStatementOfTruth(YES)
            .build();

        //Then
        assertThat(application.hasSolSignStatementOfTruth()).isTrue();
    }

    @Test
    void shouldReturnFalseIfStatementOfTruthIsNoForSolicitor() {
        //When
        final var application = Application.builder()
            .solSignStatementOfTruth(NO)
            .build();

        //Then
        assertThat(application.hasSolSignStatementOfTruth()).isFalse();
    }

    @Test
    void shouldReturnDateOfSubmissionResponseIfDateSubmittedIsSet() {
        //When
        final LocalDateTime dateSubmitted = LocalDateTime.of(2021, 8, 10, 1, 30);
        final var application = Application.builder()
            .dateSubmitted(dateSubmitted)
            .build();

        //Then
        assertThat(application.getDateOfSubmissionResponse()).isEqualTo(dateSubmitted.toLocalDate().plusDays(14));
    }

    @Test
    void shouldReturnNullIfDateSubmittedIsNotSet() {
        //When
        final var application = Application.builder()
            .build();

        //Then
        assertThat(application.getDateOfSubmissionResponse()).isNull();
    }

    @Test
    void shouldReturnTrueIfApplicant1WantsToHavePapersServedAnotherWayIsYes() {
        //When
        final var application = Application.builder()
            .applicant1WantsToHavePapersServedAnotherWay(YES)
            .build();

        //Then
        assertThat(application.hasAwaitingApplicant1Documents()).isTrue();
    }

    @Test
    void shouldReturnTrueIfApplicant1WantsToHavePapersServedAnotherWayIsNoAndApplicant1CannotUploadSupportingDocument() {
        //When
        final var application = Application.builder()
            .applicant1WantsToHavePapersServedAnotherWay(NO)
            .applicant1CannotUploadSupportingDocument(Set.of(APPLICATION))
            .build();

        //Then
        assertThat(application.hasAwaitingApplicant1Documents()).isTrue();
    }

    @Test
    void shouldReturnFalseIfApplicant1WantsToHavePapersServedAnotherWayIsNoAndEmptyApplicant1CannotUploadSupportingDocument() {
        //When
        final var application = Application.builder()
            .applicant1WantsToHavePapersServedAnotherWay(NO)
            .build();

        //Then
        assertThat(application.hasAwaitingApplicant1Documents()).isFalse();
    }

    @Test
    void shouldReturnFalseIfApplicant1WantsToHavePapersServedAnotherWayIsNull() {
        //When
        final var application = Application.builder()
            .build();

        //Then
        assertThat(application.hasAwaitingApplicant1Documents()).isFalse();
    }

    @Test
    void shouldReturnTrueIfSolicitorService() {
        //When
        final var application = Application.builder()
            .serviceMethod(SOLICITOR_SERVICE)
            .build();

        //Then
        assertThat(application.isSolicitorServiceMethod()).isTrue();
    }

    @Test
    void shouldReturnFalseIfNotSolicitorService() {
        //When
        final var application = Application.builder()
            .serviceMethod(COURT_SERVICE)
            .build();

        //Then
        assertThat(application.isSolicitorServiceMethod()).isFalse();
    }

    @Test
    void shouldReturnTrueIfPersonalService() {
        //When
        final var application = Application.builder()
            .serviceMethod(PERSONAL_SERVICE)
            .build();

        //Then
        assertThat(application.isPersonalServiceMethod()).isTrue();
    }

    @Test
    void shouldReturnFalseIfNotPersonalService() {
        //When
        final var application = Application.builder()
            .serviceMethod(COURT_SERVICE)
            .build();
        //Then
        assertThat(application.isPersonalServiceMethod()).isFalse();
    }

    @Test
    void shouldReturnTrueIfApplicant2ReminderSentIsYes() {
        //When
        final var application = Application.builder()
            .applicant2ReminderSent(YES)
            .build();

        //Then
        assertThat(application.hasApplicant2ReminderBeenSent()).isTrue();
    }

    @Test
    void shouldReturnFalseIfApplicant2ReminderSentIsNoOrNull() {
        //When
        final var application1 = Application.builder()
            .applicant2ReminderSent(NO)
            .build();
        final var application2 = Application.builder()
            .build();

        //Then
        assertThat(application1.hasApplicant2ReminderBeenSent()).isFalse();
        assertThat(application2.hasApplicant2ReminderBeenSent()).isFalse();
    }

    @Test
    void shouldReturnTrueIfApplicant1ReminderSentIsYes() {
        //When
        final var application = Application.builder()
            .applicant1ReminderSent(YES)
            .build();

        //Then
        assertThat(application.hasApplicant1ReminderBeenSent()).isTrue();
    }

    @Test
    void shouldReturnFalseIfApplicant1ReminderSentIsNoOrNull() {
        //When
        final var application1 = Application.builder()
            .applicant1ReminderSent(NO)
            .build();
        final var application2 = Application.builder()
            .build();

        //Then
        assertThat(application1.hasApplicant1ReminderBeenSent()).isFalse();
        assertThat(application2.hasApplicant1ReminderBeenSent()).isFalse();
    }

    @Test
    void shouldReturnTrueIfOverdueNotificationSentIsYes() {
        //When
        final var application = Application.builder()
            .overdueNotificationSent(YES)
            .build();

        //Then
        assertThat(application.hasOverdueNotificationBeenSent()).isTrue();
    }

    @Test
    void shouldReturnFalseIfOverdueNotificationSentIsNoOrNull() {
        //When
        final var application1 = Application.builder()
            .overdueNotificationSent(NO)
            .build();
        final var application2 = Application.builder()
            .build();

        //Then
        assertThat(application1.hasOverdueNotificationBeenSent()).isFalse();
        assertThat(application2.hasOverdueNotificationBeenSent()).isFalse();
    }

    @Test
    void shouldReturnTrueIfApplicant1HelpWithFeesNeedHelpIsSetToYes() {
        //When
        final Application application = Application.builder()
            .applicant1HelpWithFees(HelpWithFees.builder()
                .appliedForFees(YES)
                .needHelp(YES)
                .referenceNumber("number")
                .build())
            .build();
        //Then
        assertThat(application.isHelpWithFeesApplication()).isTrue();
    }

    @Test
    void shouldReturnTrueIfSolPaymentHowToPayIsSetToHelpWithFees() {
        //When
        final Application application = Application.builder()
            .solPaymentHowToPay(FEES_HELP_WITH)
            .build();

        //Then
        assertThat(application.isHelpWithFeesApplication()).isTrue();
    }

    @Test
    void shouldReturnFalseIfApplicant1HelpWithFeesNeedHelpIsSetToNo() {
        //When
        final Application application = Application.builder()
            .applicant1HelpWithFees(HelpWithFees.builder()
                .needHelp(NO)
                .build())
            .build();

        //Then
        assertThat(application.isHelpWithFeesApplication()).isFalse();
    }

    @Test
    void shouldReturnFalseIfApplicant1HelpWithFeesNeedHelpIsNull() {
        //When
        final Application application = Application.builder()
            .applicant1HelpWithFees(HelpWithFees.builder()
                .build())
            .build();

        //Then
        assertThat(application.isHelpWithFeesApplication()).isFalse();
    }

    @Test
    void shouldReturnFalseIfApplicant1HelpWithFeesIsNull() {
        //When
        final Application application = Application.builder().build();
        //Then
        assertThat(application.isHelpWithFeesApplication()).isFalse();
    }

    @Test
    void shouldReturnOptionalPbaNumberIfPbaNumberIsPresent() {
        //Given
        final String pbaNumber = "123456";
        //When
        final Application application = Application.builder()
            .pbaNumbers(DynamicList.builder()
                .value(DynamicListElement.builder().label(pbaNumber).build())
                .build())
            .build();

        //Then
        assertThat(application.getPbaNumber()).isEqualTo(Optional.of(pbaNumber));
    }

    @Test
    void shouldReturnOptionalEmptyIfPbaNumberIsNotPresent() {
        //When
        final Application application = Application.builder().build();

        //Then
        assertThat(application.getPbaNumber()).isEqualTo(Optional.empty());
    }

    private ListValue<Payment> paymentValue(final Payment payment) {
        return ListValue.<Payment>builder()
            .value(payment)
            .build();
    }

    private Payment payment(final int amount, final PaymentStatus paymentStatus) {
        return Payment.builder()
            .created(LocalDateTime.now())
            .amount(amount)
            .status(paymentStatus)
            .build();
    }
}
