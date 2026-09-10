package uk.gov.hmcts.sptribs.send35.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

/**
 * Section 3 — up to two further parents or carers to tell the tribunal about.
 *
 * <p>The form allows two. Held as two flat sets of fields rather than a collection
 * because the citizen journey asks for them one at a time and the caseworker view is a
 * tab, so a collection would add a level of nesting for no gain.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class StSend35AdditionalParents {

    @JsonProperty("AddParentOrCarer")
    @CCD(
        label = "Do you want to add an additional parent or carer?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo addParentOrCarer;

    @JsonProperty("FirstName")
    @CCD(
        label = "First name",
        showCondition = "parentsAddParentOrCarer=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String firstName;

    @JsonProperty("LastName")
    @CCD(
        label = "Last name",
        showCondition = "parentsAddParentOrCarer=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String lastName;

    @JsonProperty("PhoneNumber")
    @CCD(
        label = "Phone number",
        showCondition = "parentsAddParentOrCarer=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String phoneNumber;

    @JsonProperty("EmailAddress")
    @CCD(
        label = "Email address",
        showCondition = "parentsAddParentOrCarer=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String emailAddress;

    @JsonProperty("Relationship")
    @CCD(
        label = "Relationship to the child or young person",
        showCondition = "parentsAddParentOrCarer=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String relationship;

    @JsonProperty("AddSecondParentOrCarer")
    @CCD(
        label = "Do you want to add another parent or carer?",
        showCondition = "parentsAddParentOrCarer=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo addSecondParentOrCarer;

    @JsonProperty("SecondFirstName")
    @CCD(
        label = "First name",
        showCondition = "parentsAddSecondParentOrCarer=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String secondFirstName;

    @JsonProperty("SecondLastName")
    @CCD(
        label = "Last name",
        showCondition = "parentsAddSecondParentOrCarer=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String secondLastName;

    @JsonProperty("SecondPhoneNumber")
    @CCD(
        label = "Phone number",
        showCondition = "parentsAddSecondParentOrCarer=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String secondPhoneNumber;

    @JsonProperty("SecondEmailAddress")
    @CCD(
        label = "Email address",
        showCondition = "parentsAddSecondParentOrCarer=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String secondEmailAddress;

    @JsonProperty("SecondRelationship")
    @CCD(
        label = "Relationship to the child or young person",
        showCondition = "parentsAddSecondParentOrCarer=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String secondRelationship;
}
