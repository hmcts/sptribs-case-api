package uk.gov.hmcts.sptribs.wpaf1.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;

/**
 * Section 3 — the appellant's appointee, if they have one.
 *
 * <p>An appointee acts for an appellant who cannot act for themselves, which is a narrower
 * thing than a representative: they may only sign the notice of appeal where the appellant
 * is under 16, or cannot sign because of mental or physical infirmity. The ground is
 * captured in {@link StWpaf1SigningGround}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class StWpaf1Appointee {

    @JsonProperty("HasAppointee")
    @CCD(
        label = "Does the appellant have an appointee?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo hasAppointee;

    @JsonProperty("FirstName")
    @CCD(
        label = "First name",
        showCondition = "aptHasAppointee=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String firstName;

    @JsonProperty("LastName")
    @CCD(
        label = "Last name",
        showCondition = "aptHasAppointee=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String lastName;

    @JsonProperty("Organisation")
    @CCD(
        label = "Company or organisation",
        showCondition = "aptHasAppointee=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String organisation;

    @JsonProperty("Address")
    @CCD(
        label = "Address",
        showCondition = "aptHasAppointee=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private AddressGlobalUK address;

    @JsonProperty("PhoneNumber")
    @CCD(
        label = "Phone number",
        showCondition = "aptHasAppointee=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String phoneNumber;

    @JsonProperty("MobileNumber")
    @CCD(
        label = "Mobile number",
        showCondition = "aptHasAppointee=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String mobileNumber;

    @JsonProperty("EmailAddress")
    @CCD(
        label = "Email address",
        showCondition = "aptHasAppointee=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String emailAddress;

    @JsonProperty("SigningGround")
    @CCD(
        label = "Why the appointee is signing for the appellant",
        typeOverride = FixedRadioList,
        typeParameterOverride = "StWpaf1SigningGround",
        showCondition = "aptHasAppointee=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StWpaf1SigningGround signingGround;
}
