package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.caseworker.util.EventUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class IssueDecisionSelectRecipientsTest {

    @InjectMocks
    private IssueDecisionSelectRecipients selectRecipients;

    @Test
    void midEventReturnsNoErrors() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder()
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .build();
        final CaseIssueDecision issueDecision = CaseIssueDecision.builder().build();
        
        final CaseData caseData = CaseData.builder()
            .caseIssueDecision(issueDecision)
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);
        
        AboutToStartOrSubmitResponse<CaseData, State> response = selectRecipients.midEvent(caseDetails, caseDetails);
        
        assertThat(response.getData().getCicCase().getNotifyPartySubject().contains(SubjectCIC.SUBJECT));
        assertThat(response.getErrors().isEmpty());
    }
    
    @Test
    void midEventReturnsErrorsForMissingRecipient() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseIssueDecision decision = CaseIssueDecision.builder().build();
        
        final CaseData caseData = CaseData.builder()
            .caseIssueDecision(decision)
            .build();
        caseDetails.setData(caseData);
        
        AboutToStartOrSubmitResponse<CaseData, State> response = selectRecipients.midEvent(caseDetails, caseDetails);
        
        assertNull(response.getData().getCicCase().getNotifyPartySubject());
        assertNull(response.getData().getCicCase().getNotifyPartyRepresentative());
        assertNull(response.getData().getCicCase().getNotifyPartyRespondent());
        assertNull(response.getData().getCicCase().getNotifyPartyApplicant());
        assertThat(response.getErrors()).hasSize(1);
    }
    
    @Test
    void midEventChecksRecipients() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseDetails.setData(caseData);
        try (MockedStatic<EventUtil> mockedEventUtils = Mockito.mockStatic(EventUtil.class)) {
            mockedEventUtils.when(() -> EventUtil.checkRecipient(caseData))
                .thenReturn(Collections.emptyList());
            
            selectRecipients.midEvent(caseDetails, caseDetails);
            
            mockedEventUtils.verify(() -> EventUtil.checkRecipient(caseData), times(1));
        }
    }

}
