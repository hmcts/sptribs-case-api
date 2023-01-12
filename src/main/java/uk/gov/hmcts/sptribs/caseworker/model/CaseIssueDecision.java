package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPartiesCIC;
import uk.gov.hmcts.sptribs.ciccase.model.DecisionTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;
import uk.gov.hmcts.sptribs.document.model.CICDocument;

import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.MultiSelectList;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class CaseIssueDecision {
    @CCD(
        label = "How would you like to create the decision notice?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private NoticeOption decisionNotice;

    @CCD(
        label = "Templates",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        typeOverride = FixedList,
        typeParameterOverride = "DecisionTemplate"
    )
    private DecisionTemplate issueDecisionTemplate;

    @CCD(
        label = "Who should receive this decision notice?",
        typeOverride = MultiSelectList,
        typeParameterOverride = "ContactPartiesCIC",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Set<ContactPartiesCIC> recipients;

    @CCD(
        label = "Case Documents",
        typeOverride = Collection,
        typeParameterOverride = "CICDocument",
        access = {DefaultAccess.class}
    )
    private List<ListValue<CICDocument>> decisionDocument;
}
