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
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

/**
 * Section 11 — optional health and social care recommendations.
 *
 * <p>Recommendations rather than appeals: the tribunal can make them for any reason, and
 * the local authority sends a copy of the appeal to the health or social care provider.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class StSend35HealthAndSocialCare {

    @JsonProperty("WantRecommendation")
    @CCD(
        label = "Do you want to ask the tribunal to make a recommendation about health or social care?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo wantRecommendation;

    @JsonProperty("RecommendationTypes")
    @CCD(
        label = "What do you want the tribunal to make recommendations about?",
        typeOverride = MultiSelectList,
        typeParameterOverride = "StSend35RecommendationType",
        showCondition = "hscWantRecommendation=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private List<StSend35RecommendationType> recommendationTypes;

    @JsonProperty("HealthIssues")
    @CCD(
        label = "Issues you want the tribunal to consider and decide about health",
        typeOverride = TextArea,
        showCondition = "hscWantRecommendation=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String healthIssues;

    @JsonProperty("HealthRecommendations")
    @CCD(
        label = "Recommendations you want the tribunal to make about health",
        typeOverride = TextArea,
        showCondition = "hscWantRecommendation=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String healthRecommendations;

    @JsonProperty("SocialCareIssues")
    @CCD(
        label = "Issues you want the tribunal to consider and decide about social care",
        typeOverride = TextArea,
        showCondition = "hscWantRecommendation=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String socialCareIssues;

    @JsonProperty("SocialCareRecommendations")
    @CCD(
        label = "Recommendations you want the tribunal to make about social care",
        typeOverride = TextArea,
        showCondition = "hscWantRecommendation=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String socialCareRecommendations;
}
