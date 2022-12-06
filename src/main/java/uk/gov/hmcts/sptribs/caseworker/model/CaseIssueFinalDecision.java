package uk.gov.hmcts.sptribs.caseworker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.sptribs.ciccase.model.FinalDecisionTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CaseIssueFinalDecision {
    @CCD(
        label = "How would you like to create the decision notice?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private NoticeOption decisionNotice;

    @CCD(
        label = "Templates",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        typeOverride = FixedList,
        typeParameterOverride = "FinalDecisionTemplate"
    )
    private FinalDecisionTemplate issueFinalDecisionTemplate;
}
