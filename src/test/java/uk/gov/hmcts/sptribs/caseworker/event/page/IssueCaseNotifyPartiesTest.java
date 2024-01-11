package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.util.CheckRequiredUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.SELECT_AT_LEAST_ONE_ERROR_MESSAGE;

@ExtendWith(MockitoExtension.class)
public class IssueCaseNotifyPartiesTest {
    
    @InjectMocks
    private IssueCaseNotifyParties issueCaseNotifyParties;

    @Test
    void testMidEventReturnsErrorForNoRecipients() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = CaseData.builder().build();
        caseDetails.setData(caseData);
        
        AboutToStartOrSubmitResponse<CaseData, State> response = issueCaseNotifyParties.midEvent(caseDetails, caseDetails);
        assertFalse(response.getErrors().isEmpty());
        assertThat(response.getErrors()).contains(SELECT_AT_LEAST_ONE_ERROR_MESSAGE);
    }

    @Test
    void midEventIsSuccessfulForValidRecipients() {
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CicCase cicCase = CicCase.builder()
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .build();
        
        CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);
        
        AboutToStartOrSubmitResponse<CaseData, State> response = issueCaseNotifyParties.midEvent(caseDetails, caseDetails);
        assertTrue(response.getErrors().isEmpty());
    }
        
    @Test
    void midEventIsInvalidIfNoRecipientsSelected() {
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = CaseData.builder().build();
        caseDetails.setData(caseData);
        
        AboutToStartOrSubmitResponse<CaseData, State> response = issueCaseNotifyParties.midEvent(caseDetails, caseDetails);
        
        assertNull(response.getData().getCicCase().getNotifyPartySubject());
        assertNull(response.getData().getCicCase().getNotifyPartyRepresentative());
        assertNull(response.getData().getCicCase().getNotifyPartyRespondent());
        assertNull(response.getData().getCicCase().getNotifyPartyApplicant());
        assertThat(response.getErrors()).hasSize(1);
    }
        
    @Test
    void validateCheckNullRepresentativeRespondedIsCalledOnce() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CicCase cicCase = CicCase.builder()
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .build();
        
        CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);

        try (MockedStatic<CheckRequiredUtil> mockedCheckRequiredUtils = Mockito.mockStatic(CheckRequiredUtil.class)) {
            mockedCheckRequiredUtils.when(() -> CheckRequiredUtil.checkNullSubjectRepresentativeRespondent(caseData))
                .thenReturn(true);
            
            issueCaseNotifyParties.midEvent(caseDetails, caseDetails);
        
            mockedCheckRequiredUtils.verify(() ->  CheckRequiredUtil.checkNullSubjectRepresentativeRespondent(caseData), times(1));
        }
    }
}
