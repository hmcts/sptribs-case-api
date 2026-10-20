package uk.gov.hmcts.sptribs.wpaf1.model;

import com.fasterxml.jackson.annotation.JsonFormat;
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

import java.time.LocalDate;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;

/**
 * Section 1 — the appellant's information.
 *
 * <p>The National Insurance number is required rather than optional: rule 21(5) of the 2008
 * Rules requires the notice of appeal to carry details of the decision including its full
 * reference, and the paper form has no decision-reference field, so in practice the National
 * Insurance number and the AFCS member number are what identify the decision.
 *
 * <p>Where the appellant lives is held here because it is a fact about the appeal, not only
 * an eligibility answer.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class StWpaf1Appellant {

    @JsonProperty("LivesIn")
    @CCD(
        label = "Where does the appellant live?",
        typeOverride = FixedRadioList,
        typeParameterOverride = "StWpaf1Nation",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StWpaf1Nation livesIn;

    @JsonProperty("Title")
    @CCD(
        label = "Title",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String title;

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

    @JsonProperty("DateOfBirth")
    @CCD(
        label = "Date of birth",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @JsonProperty("NationalInsuranceNumber")
    @CCD(
        label = "National Insurance number",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String nationalInsuranceNumber;

    @JsonProperty("AfcsMemberNumber")
    @CCD(
        label = "Armed Forces Compensation Scheme member number",
        hint = "If known",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String afcsMemberNumber;

    @JsonProperty("Address")
    @CCD(
        label = "Address",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private AddressGlobalUK address;

    @JsonProperty("PhoneNumber")
    @CCD(
        label = "Phone number",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String phoneNumber;

    @JsonProperty("MobileNumber")
    @CCD(
        label = "Mobile number",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String mobileNumber;

    @JsonProperty("EmailAddress")
    @CCD(
        label = "Email address",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String emailAddress;

    @JsonProperty("ServiceStatus")
    @CCD(
        label = "Service status",
        typeOverride = FixedRadioList,
        typeParameterOverride = "StWpaf1ServiceStatus",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StWpaf1ServiceStatus serviceStatus;

    @JsonProperty("ProposedDischargeDate")
    @CCD(
        label = "Proposed discharge date",
        hint = "If known",
        showCondition = "appServiceStatus=\"stillServing\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate proposedDischargeDate;
}
