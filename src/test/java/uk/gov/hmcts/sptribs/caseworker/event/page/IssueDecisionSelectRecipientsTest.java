package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;

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
        final CicCase cicCase = CicCase.builder().notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .build();
        final CaseIssueDecision issueDecision = CaseIssueDecision.builder().build();

        final CaseData caseData = CaseData.builder()
            .caseIssueDecision(issueDecision)
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);

        //When
        final AboutToStartOrSubmitResponse<CaseData, State> response = selectRecipients.midEvent(caseDetails, caseDetails);

        //Then
        assertThat(response.getData().getCicCase().getNotifyPartySubject().contains(Set.of(SubjectCIC.SUBJECT)));
    }

    @Test
    @Disabled
    void shouldBeInvalidIfNoRecipientsSelected() {
        //Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseIssueDecision decision = CaseIssueDecision.builder().build();

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
