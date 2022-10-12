package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.sptribs.ciccase.model.HearingFormat;
import uk.gov.hmcts.sptribs.ciccase.model.HearingType;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecordListing {

    @CCD(
        label = "Hearing type",
        typeOverride = FixedRadioList,
        typeParameterOverride = "HearingType",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private HearingType hearingType;

    @CCD(
        label = "Hearing format",
        typeOverride = FixedRadioList,
        typeParameterOverride = "HearingFormat",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private HearingFormat hearingFormat;

}
