package uk.gov.hmcts.sptribs.send35.model;

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

import java.time.LocalDate;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

/**
 * Section 9 — the school, college or education provider (Section I of the plan).
 *
 * <p>Only reached when the appeal disputes Section I.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class StSend35SchoolOrProvider {

    @JsonProperty("SectionIDisagreement")
    @CCD(
        label = "What about Section I do you disagree with?",
        typeOverride = FixedRadioList,
        typeParameterOverride = "StSend35SectionIDisagreement",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StSend35SectionIDisagreement sectionIDisagreement;

    @JsonProperty("AskedForProvider")
    @CCD(
        label = "Have you asked for a specific school, college or education provider?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo askedForProvider;

    @JsonProperty("TypeOfProvider")
    @CCD(
        label = "The type of school, college or education provider you want to ask for",
        typeOverride = TextArea,
        showCondition = "schoolAskedForProvider=\"No\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String typeOfProvider;

    @JsonProperty("ProviderName")
    @CCD(
        label = "Name of school, college or education provider",
        showCondition = "schoolAskedForProvider=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String providerName;

    @JsonProperty("ProviderAddress")
    @CCD(
        label = "Address of school, college or education provider",
        showCondition = "schoolAskedForProvider=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private AddressGlobalUK providerAddress;

    @JsonProperty("DateContacted")
    @CCD(
        label = "Date you contacted them",
        showCondition = "schoolAskedForProvider=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private LocalDate dateContacted;

    @JsonProperty("ProviderResponse")
    @CCD(
        label = "Their response",
        typeOverride = TextArea,
        showCondition = "schoolAskedForProvider=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String providerResponse;
}
