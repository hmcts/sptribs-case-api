package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.caseworker.util.EventUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.SELECT_AT_LEAST_ONE_ERROR_MESSAGE;

@ExtendWith(MockitoExtension.class)
public class IssueFinalDecisionSelectRecipientsTest {

    @InjectMocks
    private SelectRecipientsHelper selectRecipients;

    @Test
    void midEventIsSuccessfulForValidRecipients() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder().notifyPartySubject(Set.of(SubjectCIC.SUBJECT)).build();
        final CaseIssueFinalDecision finalDecision = CaseIssueFinalDecision.builder().build();

        final CaseData caseData = CaseData.builder()
            .caseIssueFinalDecision(finalDecision)
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = selectRecipients.midEvent(caseDetails, caseDetails);
        assertThat(response.getData().getCicCase().getNotifyPartySubject()).contains(SubjectCIC.SUBJECT);
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void midEventReturnsErrorsForMissingRecipient() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseIssueFinalDecision finalDecision = CaseIssueFinalDecision.builder().build();
        final CicCase cicCase = CicCase.builder().build();

        final CaseData caseData = CaseData.builder()
            .caseIssueFinalDecision(finalDecision)
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = selectRecipients.midEvent(caseDetails, caseDetails);

        assertNull(response.getData().getCicCase().getNotifyPartySubject());
        assertNull(response.getData().getCicCase().getNotifyPartyRepresentative());
        assertNull(response.getData().getCicCase().getNotifyPartyRespondent());
        assertNull(response.getData().getCicCase().getNotifyPartyApplicant());
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(SELECT_AT_LEAST_ONE_ERROR_MESSAGE);
    }

    @Test
    void midEventIsSuccessfulForRepresentative() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        Set<RepresentativeCIC> representativeCICSet = new HashSet<>();
        representativeCICSet.add(RepresentativeCIC.REPRESENTATIVE);
        final CicCase cicCase = CicCase.builder()
            .notifyPartyRepresentative(representativeCICSet)
            .representativeCIC(representativeCICSet).build();

        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = selectRecipients.midEvent(caseDetails, caseDetails);

        assertThat(response.getData().getCicCase().getNotifyPartyRepresentative()).contains(RepresentativeCIC.REPRESENTATIVE);
        assertThat(response.getErrors()).isEmpty();
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
