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

import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RemoveCaseStay {

    @CCD(
        label = "Why is the stay being removed from this case?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StayRemoveReason stayRemoveReason;

    @CCD(
        label = "Provide additional details",
        typeOverride = TextArea
    )
    private String additionalDetail;

    @CCD(
        label = "Enter Other reason"
    )
    private String stayRemoveOtherDescription;
}
