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

import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

/**
 * Section 7 — anyone else holding parental responsibility.
 *
 * <p>Only asked where the child or young person is under 18. If they have not been told
 * about the appeal the tribunal needs to know why.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class StSend35ParentalResponsibility {

    @JsonProperty("OtherPersonOrOrganisation")
    @CCD(
        label = "Is there any other person or organisation with parental responsibility?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo otherPersonOrOrganisation;

    @JsonProperty("Name")
    @CCD(
        label = "Name of the other person or organisation",
        showCondition = "prOtherPersonOrOrganisation=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String name;

    @JsonProperty("Told")
    @CCD(
        label = "Have you told them about this appeal?",
        showCondition = "prOtherPersonOrOrganisation=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo told;

    @JsonProperty("ReasonNotTold")
    @CCD(
        label = "Reasons for not telling them about this appeal",
        typeOverride = TextArea,
        showCondition = "prTold=\"No\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String reasonNotTold;
}
