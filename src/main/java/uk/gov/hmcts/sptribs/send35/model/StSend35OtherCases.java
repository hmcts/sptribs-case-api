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
 * Sections 14 and 15 — other SEND appeals and other court or tribunal cases.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class StSend35OtherCases {

    @JsonProperty("OtherSendAppeals")
    @CCD(
        label = "Is the child, young person or any of their siblings involved in any other SEND appeals?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo otherSendAppeals;

    @JsonProperty("AppealReferenceNumbers")
    @CCD(
        label = "Appeal reference numbers",
        hint = "For example EH123/23/00001, EH456/56/00002",
        showCondition = "otherOtherSendAppeals=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String appealReferenceNumbers;

    @JsonProperty("OtherCourtCases")
    @CCD(
        label = "Is the child or young person involved in any other cases in another court or tribunal?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo otherCourtCases;

    @JsonProperty("CourtCaseDetails")
    @CCD(
        label = "Name and short description of the case and any orders",
        typeOverride = TextArea,
        showCondition = "otherOtherCourtCases=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String courtCaseDetails;
}
