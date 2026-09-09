package uk.gov.hmcts.sptribs.send35.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import java.util.List;

/**
 * Case data for the SEND35 case type — appeal a decision about an education, health and
 * care (EHC) plan.
 *
 * <p>This class IS the case type. {@code CCDDefinitionGenerator} groups configs by their
 * case-data class and writes one definition directory per group, so every config for this
 * case type is typed on this class and nothing else can reach it. See
 * {@code docs/adding-a-case-type.md}.
 *
 * <p>One {@code @JsonUnwrapped} complex per section of the SEND35 form, so the CCD field id
 * of a question is {@code <prefix><JsonProperty>}. The prefixes are short on purpose: CCD
 * caps field ids at 70 characters and the ids are built from the prefix, not truncated.
 * {@code StSend35CaseDataTest} asserts the budget still holds.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class StSend35CaseData {

    @JsonUnwrapped(prefix = "cyp")
    @Builder.Default
    @CCD(
        label = "Section 1 — Child or young person's information",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StSend35ChildOrYoungPerson childOrYoungPerson = new StSend35ChildOrYoungPerson();

    @JsonUnwrapped(prefix = "appellant")
    @Builder.Default
    @CCD(
        label = "Section 2 — Details of the person appealing",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StSend35Appellant appellant = new StSend35Appellant();

    @JsonUnwrapped(prefix = "parents")
    @Builder.Default
    @CCD(
        label = "Section 3 — Details of another parent or carer",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StSend35AdditionalParents additionalParents = new StSend35AdditionalParents();

    @JsonUnwrapped(prefix = "rep")
    @Builder.Default
    @CCD(
        label = "Section 4 — Representative's information",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StSend35Representative representative = new StSend35Representative();

    @JsonUnwrapped(prefix = "adv")
    @Builder.Default
    @CCD(
        label = "Section 5 — Advocate information",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StSend35Advocate advocate = new StSend35Advocate();

    @JsonUnwrapped(prefix = "comms")
    @Builder.Default
    @CCD(
        label = "Section 6 — Communication during the appeal",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StSend35Communication communication = new StSend35Communication();

    @JsonUnwrapped(prefix = "pr")
    @Builder.Default
    @CCD(
        label = "Section 7 — Anyone else with parental responsibility",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StSend35ParentalResponsibility parentalResponsibility = new StSend35ParentalResponsibility();

    @JsonUnwrapped(prefix = "appeal")
    @Builder.Default
    @CCD(
        label = "Section 8 — Type of appeal",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StSend35TypeOfAppeal typeOfAppeal = new StSend35TypeOfAppeal();

    @JsonUnwrapped(prefix = "school")
    @Builder.Default
    @CCD(
        label = "Section 9 — School, college or education provider",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StSend35SchoolOrProvider schoolOrProvider = new StSend35SchoolOrProvider();

    @JsonUnwrapped(prefix = "reasons")
    @Builder.Default
    @CCD(
        label = "Section 10 — Reasons for the appeal",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StSend35Reasons reasons = new StSend35Reasons();

    @JsonUnwrapped(prefix = "hsc")
    @Builder.Default
    @CCD(
        label = "Section 11 — Health and social care",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StSend35HealthAndSocialCare healthAndSocialCare = new StSend35HealthAndSocialCare();

    @JsonUnwrapped(prefix = "med")
    @Builder.Default
    @CCD(
        label = "Section 12 — Mediation certificate",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StSend35Mediation mediation = new StSend35Mediation();

    @JsonUnwrapped(prefix = "time")
    @Builder.Default
    @CCD(
        label = "Section 13 — Appealing on time",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StSend35Timeliness timeliness = new StSend35Timeliness();

    @JsonUnwrapped(prefix = "other")
    @Builder.Default
    @CCD(
        label = "Sections 14 and 15 — Other cases",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StSend35OtherCases otherCases = new StSend35OtherCases();

    @JsonUnwrapped(prefix = "hearing")
    @Builder.Default
    @CCD(
        label = "Section 16 — Hearing preferences",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StSend35HearingPreferences hearingPreferences = new StSend35HearingPreferences();

    @JsonUnwrapped(prefix = "support")
    @Builder.Default
    @CCD(
        label = "Section 17 — Support during your case",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StSend35Support support = new StSend35Support();

    @JsonUnwrapped(prefix = "decl")
    @Builder.Default
    @CCD(
        label = "Declaration",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StSend35Declaration declaration = new StSend35Declaration();

    @JsonProperty("SupportingEvidence")
    @CCD(
        label = "Supporting evidence",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private List<ListValue<StSend35Evidence>> supportingEvidence;
}
