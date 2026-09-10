package uk.gov.hmcts.sptribs.send35.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import java.time.LocalDate;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;

/**
 * Section 1 — the child or young person the appeal is about.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class StSend35ChildOrYoungPerson {

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
    private LocalDate dateOfBirth;

    @JsonProperty("Gender")
    @CCD(
        label = "Gender",
        typeOverride = FixedRadioList,
        typeParameterOverride = "StSend35Gender",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StSend35Gender gender;
}
