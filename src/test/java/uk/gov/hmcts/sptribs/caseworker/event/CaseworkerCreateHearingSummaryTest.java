package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.helper.RecordListHelper;
import uk.gov.hmcts.sptribs.caseworker.service.HearingService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.judicialrefdata.JudicialService;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_CREATE_HEARING_SUMMARY;

@ExtendWith(MockitoExtension.class)
class CaseworkerCreateHearingSummaryTest {

    @Mock
    private RecordListHelper recordListHelper;

    @InjectMocks
    private CaseWorkerCreateHearingSummary caseWorkerCreateHearingSummary;

    @Mock
    private HearingService hearingService;

    @Mock
    private JudicialService judicialService;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseWorkerCreateHearingSummary.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_CREATE_HEARING_SUMMARY);
    }

    @Test
    void shouldRunAboutToStart() {
        //Given
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder().build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        updatedCaseDetails.setData(caseData);
        when(hearingService.getHearingDateDynamicList(any())).thenReturn(null);
        when(judicialService.getAllUsers()).thenReturn(null);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = caseWorkerCreateHearingSummary.aboutToStart(updatedCaseDetails);

        //Then
        assertThat(response).isNotNull();
        assertThat(response.getData().getCicCase().getHearingList()).isNull();
        assertThat(response.getData().getCurrentEvent()).isEqualTo(CASEWORKER_CREATE_HEARING_SUMMARY);
    }

    @Test
    void shouldRunAboutToSubmit() {
        //Given
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder()
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        updatedCaseDetails.setData(caseData);
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseWorkerCreateHearingSummary.aboutToSubmit(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response).isNotNull();
        assertThat(response.getData().getCurrentEvent()).isBlank();
    }

    @Test
    void shouldRunSubmitted() {
        //Given
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        //When
        SubmittedCallbackResponse response =
            caseWorkerCreateHearingSummary.summaryCreated(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response).isNotNull();
        assertThat(response.getConfirmationHeader()).contains("Hearing summary created");
    }

}
