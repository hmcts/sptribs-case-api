package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonFormat;
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

import java.time.LocalDate;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Stay {

    @CCD(
        label = "Stay Reason",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StayReason stayReason;

    @CCD(
        label = "Provide additional details",
        typeOverride = TextArea
    )
    private String additionalDetail;

    @CCD(
        label = "Expiration Date"
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expirationDate;


    @CCD(
        label = "Enter a Flag TYpe",
        typeOverride = TextArea
    )
    private String flagType;
}
