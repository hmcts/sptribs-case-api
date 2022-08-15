package uk.gov.hmcts.sptribs.payment.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
public class PbaResponse {

    private HttpStatus httpStatus;

    private String errorMessage;

    private String paymentReference;
}
