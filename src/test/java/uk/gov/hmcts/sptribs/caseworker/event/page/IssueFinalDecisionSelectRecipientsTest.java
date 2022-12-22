package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.*;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class IssueFinalDecisionSelectRecipientsTest {

    @InjectMocks
    private IssueFinalDecisionSelectRecipients selectRecipients;

    @Test
    void shouldBeSuccessfulForValidRecipients() {
        //Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final Set<FinalDecisionRecipientSubjectCIC> subjectCICSet = new HashSet<>();
        subjectCICSet.add(FinalDecisionRecipientSubjectCIC.SUBJECT);
        final CicCase cicCase = CicCase.builder().recipientSubjectCIC(subjectCICSet).build();

        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);

        //When
        final AboutToStartOrSubmitResponse<CaseData, State> response = selectRecipients.midEvent(caseDetails, caseDetails);

        //Then
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldBeInvalidIfNoRecipientsSelected() {
        //Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder().recipientSubjectCIC(new HashSet<>()).build();

        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);

        //When
        final AboutToStartOrSubmitResponse<CaseData, State> response = selectRecipients.midEvent(caseDetails, caseDetails);

        //Then
        assertThat(response.getErrors()).hasSize(1);
    }

    @Test
    void shouldBeInvalidIfRepresentativeSelectedWithNoRepresentativeDetails() {
        //Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final Set<FinalDecisionRecipientRepresentativeCIC> representativeCICSet = new HashSet<>();
        representativeCICSet.add(FinalDecisionRecipientRepresentativeCIC.REPRESENTATIVE);
        final CicCase cicCase = CicCase.builder().recipientRepresentativeCIC(representativeCICSet).build();

        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);

        //When
        final AboutToStartOrSubmitResponse<CaseData, State> response = selectRecipients.midEvent(caseDetails, caseDetails);

        //Then
        assertThat(response.getErrors()).hasSize(1);
    }

    @Test
    void shouldBeValidIfRepresentativeSelectedWithRepresentativeDetails() {
        //Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final Set<FinalDecisionRecipientRepresentativeCIC> representativeCICSet = new HashSet<>();
        representativeCICSet.add(FinalDecisionRecipientRepresentativeCIC.REPRESENTATIVE);
        final Set<PartiesCIC> partiesCICSet = new HashSet<>();
        partiesCICSet.add(PartiesCIC.REPRESENTATIVE);
        final CicCase cicCase = CicCase.builder().recipientRepresentativeCIC(representativeCICSet).partiesCIC(partiesCICSet).build();

        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);

        //When
        final AboutToStartOrSubmitResponse<CaseData, State> response = selectRecipients.midEvent(caseDetails, caseDetails);

        //Then
        assertThat(response.getErrors()).isEmpty();
    }
}
