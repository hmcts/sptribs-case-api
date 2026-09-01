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
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.CriminalInjuriesCompensationData;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class PostponeHearingNotifyPartiesTest {

    @InjectMocks
    private PostponeHearingNotifyParties postponeHearingNotifyParties;

    @Test
    void testMidEventReturnsErrorForNoRecipients() {
        final CaseDetails<CriminalInjuriesCompensationData, State> caseDetails = new CaseDetails<>();
        final CriminalInjuriesCompensationData caseData = CriminalInjuriesCompensationData.builder().build();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CriminalInjuriesCompensationData,
            State> response = postponeHearingNotifyParties.midEvent(caseDetails, caseDetails);
        assertFalse(response.getErrors().isEmpty());
        assertThat(response.getErrors()).contains("At least one party must be selected.");
    }

    @Test
    void midEventIsSuccessfulForValidRecipients() {
        final CaseDetails<CriminalInjuriesCompensationData, State> caseDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder()
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .build();

        final CriminalInjuriesCompensationData caseData = CriminalInjuriesCompensationData.builder()
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CriminalInjuriesCompensationData,
            State> response = postponeHearingNotifyParties.midEvent(caseDetails, caseDetails);
        assertTrue(response.getErrors().isEmpty());
    }

    @Test
    void midEventIsInvalidIfNoRecipientsSelected() {
        final CaseDetails<CriminalInjuriesCompensationData, State> caseDetails = new CaseDetails<>();
        final CriminalInjuriesCompensationData caseData = CriminalInjuriesCompensationData.builder().build();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CriminalInjuriesCompensationData,
            State> response = postponeHearingNotifyParties.midEvent(caseDetails, caseDetails);

        assertNull(response.getData().getCicCase().getNotifyPartySubject());
        assertNull(response.getData().getCicCase().getNotifyPartyRepresentative());
        assertNull(response.getData().getCicCase().getNotifyPartyRespondent());
        assertNull(response.getData().getCicCase().getNotifyPartyApplicant());
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains("At least one party must be selected.");
    }

    @Test
    void validateCheckNullRepresentativeRespondedIsCalledOnce() {
        final CaseDetails<CriminalInjuriesCompensationData, State> caseDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder()
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .build();

        final CriminalInjuriesCompensationData caseData = CriminalInjuriesCompensationData.builder()
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);

        try (MockedStatic<CheckRequiredUtil> mockedCheckRequiredUtils = Mockito.mockStatic(CheckRequiredUtil.class)) {
            mockedCheckRequiredUtils.when(() -> CheckRequiredUtil.checkNullSubjectRepresentativeRespondent(caseData))
                .thenReturn(true);

            postponeHearingNotifyParties.midEvent(caseDetails, caseDetails);

            mockedCheckRequiredUtils.verify(() ->  CheckRequiredUtil.checkNullSubjectRepresentativeRespondent(caseData), times(1));
        }
    }
}
