package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class GeneralApplication {

    @CCD(
        label = "Choose General Application Type",
        typeOverride = FixedList,
        typeParameterOverride = "GeneralApplicationType"
    )
    private GeneralApplicationType generalApplicationType;

    @CCD(
        label = "Please provide more information about general application type",
        typeOverride = TextArea
    )
    private String generalApplicationTypeOtherComments;

    @CCD(
        label = "Choose General Application Fee Type",
        typeOverride = FixedRadioList,
        typeParameterOverride = "GeneralApplicationFee"
    )
    private GeneralApplicationFee generalApplicationFeeType;

    @CCD(
        label = "Additional comments about the supporting document",
        typeOverride = TextArea
    )
    private String generalApplicationDocumentComments;

    @JsonUnwrapped(prefix = "generalApplicationFee")
    @Builder.Default
    private FeeDetails generalApplicationFee = new FeeDetails();
}
