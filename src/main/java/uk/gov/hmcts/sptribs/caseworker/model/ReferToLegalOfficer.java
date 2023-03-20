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

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReferToLegalOfficer {

    @CCD(
        label = "Why are you referring this case?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        typeOverride = FixedList,
        typeParameterOverride = "ReferralReason"
    )
    private ReferralReason referralReason;

    @CCD(
        label = "Reason for referral",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String reasonForReferral;

    @CCD(
        label = "Provide additional details",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        typeOverride = TextArea
    )
    private String additionalInformation;

}
