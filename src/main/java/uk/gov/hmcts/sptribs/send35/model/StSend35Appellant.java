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
 * Section 2 — the person making the appeal.
 *
 * <p>WhoIsAppealing drives eligibility and most of the rest of the form: it decides
 * whether a parent or carer can be added, who may receive correspondence, and which
 * declaration applies.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class StSend35Appellant {

    @JsonProperty("WhoIsAppealing")
    @CCD(
        label = "Who is making the appeal?",
        typeOverride = FixedRadioList,
        typeParameterOverride = "StSend35WhoIsAppealing",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StSend35WhoIsAppealing whoIsAppealing;

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

    @JsonProperty("Relationship")
    @CCD(
        label = "Relationship to the child or young person",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String relationship;

    @JsonProperty("PhoneNumber")
    @CCD(
        label = "Phone number",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String phoneNumber;

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
