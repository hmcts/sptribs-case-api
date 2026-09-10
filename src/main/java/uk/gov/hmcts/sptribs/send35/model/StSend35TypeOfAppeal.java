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

import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.MultiSelectList;

/**
 * Section 8 — what is being appealed.
 *
 * <p>More than one may apply. The plan-section answers are only meaningful when the
 * appeal is about the content of an existing plan; the citizen journey enforces that and
 * the tab shows whatever was captured.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class StSend35TypeOfAppeal {

    @JsonProperty("AppealAbout")
    @CCD(
        label = "What are you appealing about?",
        typeOverride = MultiSelectList,
        typeParameterOverride = "StSend35AppealType",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private List<StSend35AppealType> appealAbout;

    @JsonProperty("FollowingAnnualReview")
    @CCD(
        label = "Is the appeal following an annual review of an EHC plan?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo followingAnnualReview;

    @JsonProperty("PlanSections")
    @CCD(
        label = "What section or sections of the EHC plan do you disagree with?",
        typeOverride = MultiSelectList,
        typeParameterOverride = "StSend35PlanSection",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private List<StSend35PlanSection> planSections;
}
