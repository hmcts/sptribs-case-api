package uk.gov.hmcts.sptribs.wpaf1.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

/**
 * Section 7 — a late appeal.
 *
 * <p>A notice received more than 12 months after the decision was sent must say why it is
 * late; it is then treated as in time unless the decision maker objects within 28 days. The
 * date the limit runs from is the decision-letter date in {@link StWpaf1Decision}, so there
 * is only one date on the form and it lives there.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class StWpaf1Timeliness {

    @JsonProperty("LateAppealExplanation")
    @CCD(
        label = "Reasons the appeal is late",
        typeOverride = TextArea,
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String lateAppealExplanation;
}
