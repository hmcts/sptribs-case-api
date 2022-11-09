package uk.gov.hmcts.sptribs.caseworker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendOrder {

    @CCD(
        label = "How would you like to issue an order?"
    )
    private OrderIssuingType orderIssuingType;

}
