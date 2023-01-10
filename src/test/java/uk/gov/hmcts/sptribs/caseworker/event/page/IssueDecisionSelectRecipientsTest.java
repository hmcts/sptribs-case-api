package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.RecipientsCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class IssueDecisionSelectRecipientsTest {

    @InjectMocks
    private IssueDecisionSelectRecipients selectRecipients;

    @Test
    void shouldBeSuccessfulForValidRecipients() {
        //Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final Set<RecipientsCIC> subjectSet = new HashSet<>();
        subjectSet.add(RecipientsCIC.SUBJECT);
        final CaseIssueDecision decision = CaseIssueDecision.builder().recipients(subjectSet).build();

        final CaseData caseData = CaseData.builder()
            .caseIssueDecision(decision)
            .build();
        caseDetails.setData(caseData);

        //When
        final AboutToStartOrSubmitResponse<CaseData, State> response = selectRecipients.midEvent(caseDetails, caseDetails);

        //Then
        assertThat(response.getData().getCaseIssueDecision().getRecipients().equals(RecipientsCIC.SUBJECT));
    }

    @Test
    void shouldBeInvalidIfNoRecipientsSelected() {
        //Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseIssueDecision decision = CaseIssueDecision.builder().recipients(new HashSet<>()).build();

        final CaseData caseData = CaseData.builder()
            .caseIssueDecision(decision)
            .build();
        caseDetails.setData(caseData);

        //When
        final AboutToStartOrSubmitResponse<CaseData, State> response = selectRecipients.midEvent(caseDetails, caseDetails);

        //Then
        assertThat(response.getErrors()).hasSize(1);
    }

}
