package uk.gov.hmcts.sptribs.edgecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.sptribs.edgecase.model.access.CollectionAccess;
import uk.gov.hmcts.sptribs.edgecase.model.access.DefaultAccess;
import uk.gov.hmcts.sptribs.model.PaymentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Application {
    @CCD(ignore = true)
    private static final int SUBMISSION_RESPONSE_DAYS = 14;

    @CCD(label = "Date when the application was created", access = { DefaultAccess.class })
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdDate;

    @CCD(label = "Date submitted to HMCTS", access = { DefaultAccess.class })
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime dateSubmitted;

    @CCD(label = "Here are your order details", access = { CollectionAccess.class })
    private OrderSummary applicationFeeOrderSummary;

    @CCD(label = "Payments", typeOverride = Collection, typeParameterOverride = "Payment",
            access = { CollectionAccess.class })
    private List<ListValue<Payment>> applicationPayments;

    @JsonIgnore
    public PaymentStatus getLastPaymentStatus() {
        return applicationPayments == null || applicationPayments.isEmpty()
                ? null
                : applicationPayments.get(applicationPayments.size() - 1).getValue().getStatus();
    }

    @JsonIgnore
    public boolean hasBeenPaidFor() {
        return null != applicationFeeOrderSummary
                && Integer.parseInt(applicationFeeOrderSummary.getPaymentTotal()) == getPaymentTotal();
    }

    @JsonIgnore
    public Integer getPaymentTotal() {
        return applicationPayments == null
                ? 0
                : applicationPayments
                        .stream()
                        .filter(p -> p.getValue().getStatus().equals(PaymentStatus.SUCCESS))
                        .map(p -> p.getValue().getAmount())
                        .reduce(0, Integer::sum);
    }

    @JsonIgnore
    public LocalDate getDateOfSubmissionResponse() {
        return dateSubmitted == null ? null : dateSubmitted.plusDays(SUBMISSION_RESPONSE_DAYS).toLocalDate();
    }
}
