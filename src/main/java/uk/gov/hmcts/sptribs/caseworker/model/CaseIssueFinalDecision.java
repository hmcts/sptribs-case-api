package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.ciccase.model.FinalDecisionRecipientRepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.FinalDecisionRecipientRespondentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.FinalDecisionRecipientSubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.FinalDecisionTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import java.util.Set;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.MultiSelectList;

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
        label = "Final Decision Templates",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        typeOverride = FixedList,
        typeParameterOverride = "FinalDecisionTemplate"
    )
    private FinalDecisionTemplate finalDecisionTemplate;

    @CCD(
        label = "Final decision notice preview",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Document finalDecisionDraft;

    @CCD(
        label = "Decision notice recipient",
        typeOverride = MultiSelectList,
        typeParameterOverride = "FinalDecisionRecipientSubjectCIC",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Set<FinalDecisionRecipientSubjectCIC> recipientSubjectCIC;

    @CCD(
        label = "Decision notice recipient",
        typeOverride = MultiSelectList,
        typeParameterOverride = "FinalDecisionRecipientRepresentativeCIC",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Set<FinalDecisionRecipientRepresentativeCIC> recipientRepresentativeCIC;

    @CCD(
        label = "Decision notice recipient",
        typeOverride = MultiSelectList,
        typeParameterOverride = "FinalDecisionRecipientRespondentCIC",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Set<FinalDecisionRecipientRespondentCIC> recipientRespondentCIC;
}
