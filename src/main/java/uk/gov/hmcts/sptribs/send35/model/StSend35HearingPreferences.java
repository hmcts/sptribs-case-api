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

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

/**
 * Section 16 — how the appellant would like the appeal decided.
 *
 * <p>A preference, not an election: a paper hearing happens only if the local authority
 * also agrees, and the tribunal decides the format.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class StSend35HearingPreferences {

    @JsonProperty("PreferredType")
    @CCD(
        label = "What type of hearing would you prefer?",
        typeOverride = FixedRadioList,
        typeParameterOverride = "StSend35HearingType",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private StSend35HearingType preferredType;

    @JsonProperty("WantEarlierHearing")
    @CCD(
        label = "Do you want an earlier hearing if one becomes available?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo wantEarlierHearing;

    @JsonProperty("CanAttendByVideo")
    @CCD(
        label = "Are you able to take part in hearings by video?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo canAttendByVideo;

    @JsonProperty("CannotAttendByVideoReason")
    @CCD(
        label = "Why you cannot attend a hearing by video",
        typeOverride = TextArea,
        showCondition = "hearingCanAttendByVideo=\"No\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String cannotAttendByVideoReason;
}
