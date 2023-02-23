package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.ciccase.model.PartiesCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.service.SubmissionService;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_EDIT_CASE;

@ExtendWith(MockitoExtension.class)
class CaseworkerEditCaseTest {
    @InjectMocks
    private CaseworkerEditCase caseworkerEditCase;

    @Mock
    private SubmissionService submissionService;

    @BeforeEach
    public void setUp() {
        caseworkerEditCase = new CaseworkerEditCase(submissionService);
    }


    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //when
        caseworkerEditCase.configure(configBuilder);

        //then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_EDIT_CASE);
    }

    @Test
    void shouldSuccessfullyEditCase() {
        //Given
        final CaseData caseData = caseData();
        caseData.setNote("This is a test note");
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setData(caseData);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setState(State.CaseClosed);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        when(submissionService.submitApplication(any())).thenReturn(updatedCaseDetails);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerEditCase.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse stayedResponse = caseworkerEditCase.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response.getData()).isNotNull();
        assertThat(stayedResponse).isNotNull();
    }


    @Test
    void shouldSuccessfullyEditCaseRemoveRepresentative() {
        //Given
        final CaseData afterData = caseData();
        final CaseData beforeData = caseData();
        Set<NotificationParties> beforeNotificationParties = new HashSet<>();
        beforeNotificationParties.add(NotificationParties.SUBJECT);
        beforeNotificationParties.add(NotificationParties.REPRESENTATIVE);
        Set<PartiesCIC> parties = new HashSet<>();
        parties.add(PartiesCIC.SUBJECT);
        parties.add(PartiesCIC.APPLICANT);
        parties.add(PartiesCIC.REPRESENTATIVE);
        final CicCase beforeCicCase = CicCase.builder()
            .representativeContactDetailsPreference(ContactPreferenceType.POST)
            .representativeAddress(SOLICITOR_ADDRESS)
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .hearingNotificationParties(beforeNotificationParties)
            .partiesCIC(parties)
            .build();
        Set<PartiesCIC> newParties = new HashSet<>();
        newParties.add(PartiesCIC.SUBJECT);
        final CicCase newCicCase = CicCase.builder()
            .partiesCIC(newParties)
            .build();
        afterData.setCicCase(newCicCase);
        beforeData.setCicCase(beforeCicCase);
        afterData.setNote("This is a test note");
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setData(beforeData);
        updatedCaseDetails.setData(afterData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        when(submissionService.submitApplication(any())).thenReturn(updatedCaseDetails);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerEditCase.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse stayedResponse = caseworkerEditCase.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response.getData()).isNotNull();
        assertThat(stayedResponse).isNotNull();
    }

    @Test
    void shouldSuccessfullyEditCaseRemoveApplicant() {
        //Given
        final CaseData afterData = caseData();
        final CaseData beforeData = caseData();
        Set<NotificationParties> beforeNotificationParties = new HashSet<>();
        beforeNotificationParties.add(NotificationParties.SUBJECT);
        beforeNotificationParties.add(NotificationParties.APPLICANT);
        Set<PartiesCIC> parties = new HashSet<>();
        parties.add(PartiesCIC.SUBJECT);
        parties.add(PartiesCIC.APPLICANT);
        final CicCase beforeCicCase = CicCase.builder()
            .applicantContactDetailsPreference(ContactPreferenceType.POST)
            .applicantFullName(APPLICANT_2_FIRST_NAME)
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .hearingNotificationParties(beforeNotificationParties)
            .partiesCIC(parties)
            .build();
        Set<PartiesCIC> newParties = new HashSet<>();
        newParties.add(PartiesCIC.SUBJECT);
        final CicCase newCicCase = CicCase.builder()
            .partiesCIC(newParties)
            .build();
        afterData.setCicCase(newCicCase);
        beforeData.setCicCase(beforeCicCase);
        afterData.setNote("This is a test note");
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setData(beforeData);
        updatedCaseDetails.setData(afterData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        when(submissionService.submitApplication(any())).thenReturn(updatedCaseDetails);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerEditCase.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse stayedResponse = caseworkerEditCase.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response.getData()).isNotNull();
        assertThat(stayedResponse).isNotNull();
    }

}
