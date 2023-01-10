package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.RecipientsCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.ciccase.model.RecipientsCIC.REPRESENTATIVE;
import static uk.gov.hmcts.sptribs.ciccase.model.RecipientsCIC.RESPONDENT;
import static uk.gov.hmcts.sptribs.ciccase.model.RecipientsCIC.SUBJECT;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_ISSUE_DECISION;

@ExtendWith(MockitoExtension.class)
class CaseworkerIssueDecisionTest {

    @InjectMocks
    private CaseWorkerIssueDecision issueDecision;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        issueDecision.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_ISSUE_DECISION);
    }

    @Test
    void shouldSuccessfullyAddDecisionRecipients() {
        //Given
        final CaseData caseData = caseData();
        final CaseIssueDecision decision = caseData.getCaseIssueDecision();
        Set<RecipientsCIC> subjectSet = new HashSet<>();
        subjectSet.add(RecipientsCIC.SUBJECT);
        subjectSet.add(RecipientsCIC.REPRESENTATIVE);
        subjectSet.add(RecipientsCIC.RESPONDENT);
        decision.setRecipients(subjectSet);

        //Then
        assertThat(caseData.getCaseIssueDecision().getRecipients().contains(SUBJECT));
        assertThat(caseData.getCaseIssueDecision().getRecipients().contains(REPRESENTATIVE));
        assertThat(caseData.getCaseIssueDecision().getRecipients().contains(RESPONDENT));
    }

    @Test
    void shouldSetState() {
        //Given
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final CaseData caseData = caseData();
        details.setData(caseData);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = issueDecision.aboutToSubmit(details, beforeDetails);

        //Then
        assertThat(response.getState())
            .isEqualTo(CaseManagement);
    }
}
