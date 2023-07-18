package uk.gov.hmcts.sptribs.notifyproxy.dtos;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderMethodName = "personalisationRequestWith")
public class Personalisation {

    @NotNull(message = "ccdCaseNumber cannot be null")
    @NotEmpty(message = "ccdCaseNumber cannot be blank")
    private String ccdCaseNumber;

    private String refundReference;

    @NotNull(message = "Refund amount cannot be null")
    private BigDecimal refundAmount;

    @NotNull(message = "Refund reason cannot be null")
    @NotEmpty(message = "Refund reason cannot be blank")
    private String refundReason;
}
