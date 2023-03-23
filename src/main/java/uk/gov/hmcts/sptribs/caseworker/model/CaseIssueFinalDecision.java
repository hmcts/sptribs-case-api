package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.ciccase.model.DecisionTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;
import uk.gov.hmcts.sptribs.document.model.CICDocument;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class CaseIssueFinalDecision {
    @CCD(
        label = "How would you like to create the decision notice?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private NoticeOption finalDecisionNotice;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        typeOverride = FixedList,
        typeParameterOverride = "DecisionTemplate"
    )
    private DecisionTemplate decisionTemplate;

    @CCD(
        label = "Final Decision Document",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Document finalDecisionDraft;

    @CCD(
        label = "Final decision notice preview",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Document finalDecisionGuidance;

    @CCD(
        label = "Final Decision Document",
        typeParameterOverride = "CICDocument",
        access = {DefaultAccess.class}
    )
    private CICDocument document;
}
