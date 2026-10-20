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
 * Section 5 — the reasons for appealing.
 *
 * <p>These are the grounds relied on, which rule 21(5) requires the notice of appeal to
 * state.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class StWpaf1Reasons {

    @JsonProperty("AppealReasons")
    @CCD(
        label = "Why does the appellant disagree with the decision?",
        typeOverride = TextArea,
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String appealReasons;
}
