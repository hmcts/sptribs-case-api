package uk.gov.hmcts.sptribs.send35.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;

/**
 * Section 6 — the single point of contact for the appeal.
 *
 * <p>The tribunal provides information to one person only, and it must be someone named
 * elsewhere on this form.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class StSend35Communication {

    @JsonProperty("Recipient")
    @CCD(
        label = "Who do you want to receive information about the appeal?",
        typeOverride = FixedRadioList,
        typeParameterOverride = "StSend35RecipientOfInformation",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StSend35RecipientOfInformation recipient;

    @JsonProperty("FirstName")
    @CCD(
        label = "First name",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String firstName;

    @JsonProperty("LastName")
    @CCD(
        label = "Last name",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String lastName;

    @JsonProperty("MobileNumber")
    @CCD(
        label = "Mobile phone number",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String mobileNumber;

    @JsonProperty("EmailAddress")
    @CCD(
        label = "Email address",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String emailAddress;

    @JsonProperty("Address")
    @CCD(
        label = "Address",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private AddressGlobalUK address;
}
