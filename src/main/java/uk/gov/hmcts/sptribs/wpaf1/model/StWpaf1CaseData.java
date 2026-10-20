package uk.gov.hmcts.sptribs.wpaf1.model;

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
 * Case data for the WPAF1 case type — appeal a War Pension or Armed Forces Compensation
 * Scheme decision.
 *
 * <p>This class IS the case type. {@code CCDDefinitionGenerator} groups configs by their
 * case-data class and writes one definition directory per group, so every config for this
 * case type is typed on this class and nothing else can reach it. See
 * {@code docs/adding-a-case-type.md}.
 *
 * <p>One {@code @JsonUnwrapped} complex per section of the WPAF1 form, so the CCD field id
 * of a question is {@code <prefix><JsonProperty>}. The prefixes are short on purpose: CCD
 * caps field ids at 70 characters and the ids are built from the prefix, not truncated.
 * {@code StWpaf1CaseDataTest} asserts the budget still holds.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class StWpaf1CaseData {

    @JsonUnwrapped(prefix = "app")
    @Builder.Default
    @CCD(
        label = "Section 1 — Appellant's information",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StWpaf1Appellant appellant = new StWpaf1Appellant();

    @JsonUnwrapped(prefix = "rep")
    @Builder.Default
    @CCD(
        label = "Section 2 — Appellant's representative",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StWpaf1Representative representative = new StWpaf1Representative();

    @JsonUnwrapped(prefix = "apt")
    @Builder.Default
    @CCD(
        label = "Section 3 — Appellant's appointee",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StWpaf1Appointee appointee = new StWpaf1Appointee();

    @JsonUnwrapped(prefix = "dsn")
    @Builder.Default
    @CCD(
        label = "Section 4 — Decision appealed against",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StWpaf1Decision decision = new StWpaf1Decision();

    @JsonUnwrapped(prefix = "rsn")
    @Builder.Default
    @CCD(
        label = "Section 5 — Reasons for appealing",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StWpaf1Reasons reasons = new StWpaf1Reasons();

    @JsonUnwrapped(prefix = "rec")
    @Builder.Default
    @CCD(
        label = "Section 6 — AFCS reconsideration",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StWpaf1Reconsideration reconsideration = new StWpaf1Reconsideration();

    @JsonUnwrapped(prefix = "time")
    @Builder.Default
    @CCD(
        label = "Section 7 — Late appeals",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StWpaf1Timeliness timeliness = new StWpaf1Timeliness();

    @JsonUnwrapped(prefix = "sup")
    @Builder.Default
    @CCD(
        label = "Section 8 — Reasonable adjustments",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StWpaf1Support support = new StWpaf1Support();

    @JsonUnwrapped(prefix = "uksf")
    @Builder.Default
    @CCD(
        label = "UK Special Forces confidentiality",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StWpaf1Uksf uksf = new StWpaf1Uksf();

    @JsonUnwrapped(prefix = "decl")
    @Builder.Default
    @CCD(
        label = "Declaration",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StWpaf1Declaration declaration = new StWpaf1Declaration();

    @JsonProperty("SupportingEvidence")
    @CCD(
        label = "Additional evidence",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private List<ListValue<StWpaf1Evidence>> supportingEvidence;
}
