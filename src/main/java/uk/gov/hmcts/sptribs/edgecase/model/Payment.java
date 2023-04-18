package uk.gov.hmcts.sptribs.edgecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.sptribs.model.PaymentStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
@Builder
public class Payment {
    @CCD(
        label = "Created date"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime created;

    @CCD(
        label = "Updated date"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime updated;

    @CCD(
        label = "fee code"
    )
    private String feeCode;

    @CCD(
        label = "Amount in pounds"
    )
    private Integer amount;

    @CCD(
        label = "Status"
    )
    private PaymentStatus status;

    @CCD(
        label = "Channel"
    )
    private String channel;

    @CCD(
        label = "Reference"
    )
    private String reference;

    @CCD(
        label = "Transaction Id"
    )
    private String transactionId;
}
