package uk.gov.hmcts.sptribs.wpaf1.model;

import com.fasterxml.jackson.annotation.JsonFormat;
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
import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.MultiSelectList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

/**
 * Section 4 — the decision being appealed against.
 *
 * <p>The decision-letter date is the single most consequential answer on the form. Rule 21
 * gives 12 months from the date the decision was sent, rule 21(2) admits a late appeal on
 * reasons, and rule 21(4) bars any appeal more than 24 months out — a limit the tribunal has
 * no power to extend.
 *
 * <p>The prefix is {@code dsn} rather than {@code dec} so the field ids cannot be misread
 * next to the declaration's {@code decl}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class StWpaf1Decision {

    @JsonProperty("Scheme")
    @CCD(
        label = "Which scheme was the decision made under?",
        typeOverride = FixedRadioList,
        typeParameterOverride = "StWpaf1Scheme",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StWpaf1Scheme scheme;

    @JsonProperty("DecisionLetterDate")
    @CCD(
        label = "Date of the decision letter",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate decisionLetterDate;

    @JsonProperty("AppealAbout")
    @CCD(
        label = "What is the appeal about?",
        typeOverride = MultiSelectList,
        typeParameterOverride = "StWpaf1AppealType",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private List<StWpaf1AppealType> appealAbout;

    @JsonProperty("AppealAboutOther")
    @CCD(
        label = "What else is the appeal about?",
        typeOverride = TextArea,
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String appealAboutOther;
}
