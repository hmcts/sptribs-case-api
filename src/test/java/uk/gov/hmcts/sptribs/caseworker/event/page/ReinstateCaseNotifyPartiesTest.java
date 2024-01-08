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
import uk.gov.hmcts.sptribs.caseworker.util.EventUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ReinstateCaseNotifyPartiesTest {

    @InjectMocks
    private ReinstateNotifyParties reinstateNotifyParties;

    private CaseDetails<CaseData, State> caseDetails;

    @BeforeEach
    void setUp() {
        this.caseDetails = new CaseDetails<>();
    }

    @Test
    void midEventSuccessfulForValidRecipients() {
        final CicCase cicCase = CicCase.builder().notifyPartySubject(Set.of(SubjectCIC.SUBJECT)).build();

        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        this.caseDetails.setData(caseData);
 
        AboutToStartOrSubmitResponse<CaseData, State> response = reinstateNotifyParties.midEvent(this.caseDetails, this.caseDetails);
        assertTrue(response.getErrors().isEmpty());
    }

    @Test
    void shouldReturnErrorsIfNoNotificationPartySelected() {
        final CaseData caseData = CaseData.builder().build();
        this.caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = reinstateNotifyParties.midEvent(this.caseDetails, this.caseDetails);

        assertNull(response.getData().getCicCase().getNotifyPartySubject());
        assertNull(response.getData().getCicCase().getNotifyPartyRepresentative());
        assertNull(response.getData().getCicCase().getNotifyPartyRespondent());
        assertNull(response.getData().getCicCase().getNotifyPartyApplicant());
        assertThat(response.getErrors()).hasSize(1);
    }

    @Test
    void midEventChecksRecipients() {
        CaseData caseData = CaseData.builder().build();
        this.caseDetails.setData(caseData);
        try (MockedStatic<EventUtil> mockedEventUtils = Mockito.mockStatic(EventUtil.class)) {
            mockedEventUtils.when(() -> EventUtil.checkRecipient(caseData))
                .thenReturn(Collections.emptyList());
            
            reinstateNotifyParties.midEvent(this.caseDetails, this.caseDetails);
            
            mockedEventUtils.verify(() ->  EventUtil.checkRecipient(caseData), times(1));
        }
    }
}
