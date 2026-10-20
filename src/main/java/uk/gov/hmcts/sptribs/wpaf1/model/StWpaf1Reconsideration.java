package uk.gov.hmcts.sptribs.wpaf1.model;

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
 * Section 6 — whether a Reconsideration has been asked for.
 *
 * <p>Armed Forces Compensation Scheme appeals only. A Reconsideration is a legal
 * requirement before the appeal can be heard, so a "No" here means the tribunal must refer
 * the form to Defence Business Services to reconsider first. It does not stop the appeal,
 * which is why the answer is recorded rather than used as a gate.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class StWpaf1Reconsideration {

    @JsonProperty("AskedForReconsideration")
    @CCD(
        label = "Has the appellant asked the Secretary of State to look at the decision again?",
        showCondition = "dsnScheme=\"afcs\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo askedForReconsideration;
}
