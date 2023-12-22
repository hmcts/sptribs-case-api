package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.BeforeEach;
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
    private CaseDetails<CaseData, State> caseDetails;

    @BeforeEach
    void setUp() {
        this.caseDetails = new CaseDetails<>();
    }

    @Test
    void midEventReturnsNoErrors() {
        CicCase cicCase = CicCase.builder().notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .build();
        CaseIssueDecision issueDecision = CaseIssueDecision.builder().build();

        CaseData caseData = CaseData.builder()
            .caseIssueDecision(issueDecision)
            .cicCase(cicCase)
            .build();
        this.caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = selectRecipients.midEvent(this.caseDetails, this.caseDetails);

        assertThat(response.getData().getCicCase().getNotifyPartySubject().contains(SubjectCIC.SUBJECT));
        assertThat(response.getErrors().isEmpty());
    }

    @Test
    void midEventReturnsErrorsForMissingRecipient() {
        CaseIssueDecision decision = CaseIssueDecision.builder().build();

        CaseData caseData = CaseData.builder()
            .caseIssueDecision(decision)
            .build();
        this.caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = selectRecipients.midEvent(this.caseDetails, this.caseDetails);

        assertNull(response.getData().getCicCase().getNotifyPartySubject());
        assertThat(response.getErrors()).hasSize(1);
    }

    @Test
    void midEventChecksRecipients() {
        CaseData caseData = CaseData.builder().build();
        this.caseDetails.setData(caseData);
        try (MockedStatic<EventUtil> mockedEventUtils = Mockito.mockStatic(EventUtil.class)) {
            mockedEventUtils.when(() -> EventUtil.checkRecipient(caseData))
                .thenReturn(Collections.emptyList());
            
            selectRecipients.midEvent(this.caseDetails, this.caseDetails);
            
            mockedEventUtils.verify(() -> EventUtil.checkRecipient(caseData), times(1));
        }
    }

}
