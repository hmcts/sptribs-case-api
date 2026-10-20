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

/**
 * Section 2 — the appellant's representative, if they have one.
 *
 * <p>A representative need not be legally qualified. Naming one, and signing the form,
 * authorises the tribunal and Veterans UK to discuss the appeal with them, so the contact
 * details here are the ones correspondence goes to.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class StWpaf1Representative {

    @JsonProperty("HasRepresentative")
    @CCD(
        label = "Does the appellant have a representative?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo hasRepresentative;

    @JsonProperty("FirstName")
    @CCD(
        label = "First name",
        showCondition = "repHasRepresentative=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String firstName;

    @JsonProperty("LastName")
    @CCD(
        label = "Last name",
        showCondition = "repHasRepresentative=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String lastName;

    @JsonProperty("Organisation")
    @CCD(
        label = "Company or organisation",
        showCondition = "repHasRepresentative=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String organisation;

    @JsonProperty("Address")
    @CCD(
        label = "Address",
        showCondition = "repHasRepresentative=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private AddressGlobalUK address;

    @JsonProperty("DxNumber")
    @CCD(
        label = "DX number",
        showCondition = "repHasRepresentative=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String dxNumber;

    @JsonProperty("ReferenceNumber")
    @CCD(
        label = "Representative's reference number",
        showCondition = "repHasRepresentative=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String referenceNumber;

    @JsonProperty("PhoneNumber")
    @CCD(
        label = "Phone number",
        showCondition = "repHasRepresentative=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String phoneNumber;

    @JsonProperty("MobileNumber")
    @CCD(
        label = "Mobile number",
        showCondition = "repHasRepresentative=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String mobileNumber;

    @JsonProperty("EmailAddress")
    @CCD(
        label = "Email address",
        showCondition = "repHasRepresentative=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String emailAddress;
}
