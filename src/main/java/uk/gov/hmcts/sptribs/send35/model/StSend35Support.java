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
 * Section 17 — interpreters and reasonable adjustments.
 *
 * <p>Covers all communication with the tribunal, not only the hearing.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class StSend35Support {

    @JsonProperty("NeedsInterpreter")
    @CCD(
        label = "Do you, or anyone supporting you, need a spoken language interpreter?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo needsInterpreter;

    @JsonProperty("Languages")
    @CCD(
        label = "Languages and dialects needed",
        showCondition = "supportNeedsInterpreter=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String languages;

    @JsonProperty("NeedsAdjustments")
    @CCD(
        label = "Do you, or anyone supporting you, need any reasonable adjustments or support?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo needsAdjustments;

    @JsonProperty("AdjustmentsDetail")
    @CCD(
        label = "Support being asked for",
        typeOverride = TextArea,
        showCondition = "supportNeedsAdjustments=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String adjustmentsDetail;
}
