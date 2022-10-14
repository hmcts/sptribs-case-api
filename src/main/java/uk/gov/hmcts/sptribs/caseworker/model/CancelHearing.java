package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.MultiSelectList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CancelHearing {

    @CCD(
        label = "Enter any other important information about this cancellation",
        typeOverride = TextArea
    )
    private String additionalDetail;


    @CCD(
        label = "Why was the hearing cancelled?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        typeOverride = MultiSelectList,
        typeParameterOverride = "HearingCancellationReason"
    )
    private HearingCancellationReason hearingCancellationReason;

}
