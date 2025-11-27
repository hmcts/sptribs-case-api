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
import uk.gov.hmcts.ccd.sdk.type.Flags;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.ciccase.model.PartiesCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RegionCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.access.Permissions;
import uk.gov.hmcts.sptribs.common.service.SubmissionService;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.APPLICANT_FIRST_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_FIRST_NAME;
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
    void shouldAddPublishToCamundaWhenWAIsEnabled() {

        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerEditCase.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_EDIT_CASE);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::isPublishToCamunda)
            .contains(true);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::getGrants)
                .extracting(map -> map.containsKey(ST_CIC_WA_CONFIG_USER))
                .contains(true);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::getGrants)
                .extracting(map -> map.get(ST_CIC_WA_CONFIG_USER))
                .contains(Permissions.CREATE_READ_UPDATE);
    }

    @Test
    void shouldSuccessfullyEditDssCase() {
        //Given
        final CaseData caseData = caseData();
        caseData.setNote("This is a test note");
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setData(caseData);
        beforeDetails.setState(State.DSS_Submitted);
        Set<PartiesCIC> parties = new HashSet<>();
        parties.add(PartiesCIC.SUBJECT);
        parties.add(PartiesCIC.APPLICANT);
        caseData.getCicCase().setPartiesCIC(parties);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        when(submissionService.submitApplication(any())).thenReturn(updatedCaseDetails);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerEditCase.aboutToSubmit(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response.getData()).isNotNull();
        assertThat(response.getState()).isEqualTo(State.Submitted);
    }

    @Test
    void shouldSuccessfullyEditCase() {
        //Given
        final CaseData caseData = caseData();
        caseData.setNote("This is a test note");
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setData(caseData);
        Set<PartiesCIC> parties = new HashSet<>();
        parties.add(PartiesCIC.SUBJECT);
        parties.add(PartiesCIC.APPLICANT);
        caseData.getCicCase().setPartiesCIC(parties);
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
        SubmittedCallbackResponse editedResponse = caseworkerEditCase.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response.getData()).isNotNull();
        assertThat(editedResponse).isNotNull();
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
            .applicantFullName(APPLICANT_FIRST_NAME)
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
    void shouldSuccessfullyEditCaseUpdateRegion() {
        //Given
        final CaseData afterData = caseData();
        final CaseData beforeData = caseData();

        final CicCase beforeCicCase = CicCase.builder()
            .regionCIC(RegionCIC.SCOTLAND)
            .build();

        Set<PartiesCIC> parties = new HashSet<>();
        parties.add(PartiesCIC.SUBJECT);
        parties.add(PartiesCIC.APPLICANT);
        final CicCase newCicCase = CicCase.builder()
            .regionCIC(RegionCIC.LONDON)
            .partiesCIC(parties)
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
        SubmittedCallbackResponse editedResponse = caseworkerEditCase.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getCicCase().getRegionCIC().getLabel()).isEqualTo(RegionCIC.LONDON.getLabel());
        assertThat(editedResponse).isNotNull();
    }

    @Test
    void shouldSuccessfullyInitialiseFlagsWithNullCaseFlags() {
        //Given
        final CaseData afterData = caseData();
        final CaseData beforeData = caseData();

        final CicCase beforeCicCase = CicCase.builder()
            .build();
        Set<PartiesCIC> newParties = new HashSet<>();
        newParties.add(PartiesCIC.SUBJECT);
        final CicCase newCicCase = CicCase.builder()
            .partiesCIC(newParties)
            .fullName(TEST_FIRST_NAME)
            .applicantFullName(APPLICANT_FIRST_NAME)
            .representativeFullName(TEST_FIRST_NAME)
            .build();
        afterData.setCicCase(newCicCase);
        afterData.setCaseFlags(null);
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

        //Then
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getCaseFlags()).isNotNull();
        assertThat(response.getData().getSubjectFlags()).isNotNull();
        assertThat(response.getData().getApplicantFlags()).isNull();
        assertThat(response.getData().getRepresentativeFlags()).isNull();
    }

    @Test
    void shouldSuccessfullyInitialiseFlagsWithApplicant() {
        //Given
        final CaseData afterData = caseData();
        final CaseData beforeData = caseData();

        final CicCase beforeCicCase = CicCase.builder()
            .build();
        Set<PartiesCIC> newParties = new HashSet<>();
        newParties.add(PartiesCIC.SUBJECT);
        newParties.add(PartiesCIC.APPLICANT);
        final CicCase newCicCase = CicCase.builder()
            .partiesCIC(newParties)
            .fullName(TEST_FIRST_NAME)
            .applicantFullName(APPLICANT_FIRST_NAME)
            .representativeFullName(TEST_FIRST_NAME)
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

        //Then
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getCaseFlags()).isNotNull();
        assertThat(response.getData().getSubjectFlags()).isNotNull();
        assertThat(response.getData().getApplicantFlags()).isNotNull();
        assertThat(response.getData().getRepresentativeFlags()).isNull();
    }

    @Test
    void shouldSuccessfullyInitialiseFlagsWithNullApplicantDetails() {
        //Given
        final CaseData afterData = caseData();
        final CaseData beforeData = caseData();

        final CicCase beforeCicCase = CicCase.builder()
            .build();
        Set<PartiesCIC> newParties = new HashSet<>();
        newParties.add(PartiesCIC.SUBJECT);
        newParties.add(PartiesCIC.APPLICANT);
        final CicCase newCicCase = CicCase.builder()
            .partiesCIC(newParties)
            .fullName(TEST_FIRST_NAME)
            .applicantFullName(APPLICANT_FIRST_NAME)
            .representativeFullName(TEST_FIRST_NAME)
            .build();
        afterData.setApplicantFlags(Flags.builder().build());
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

        //Then
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getCaseFlags()).isNotNull();
        assertThat(response.getData().getSubjectFlags()).isNotNull();
        assertThat(response.getData().getApplicantFlags()).isNotNull();
        assertThat(response.getData().getRepresentativeFlags()).isNull();
    }

    @Test
    void shouldSuccessfullyInitialiseFlagsScenarioWithRepresentative() {
        //Given
        final CaseData afterData = caseData();
        final CaseData beforeData = caseData();

        final CicCase beforeCicCase = CicCase.builder()
            .build();
        Set<PartiesCIC> newParties = new HashSet<>();
        newParties.add(PartiesCIC.SUBJECT);
        newParties.add(PartiesCIC.REPRESENTATIVE);
        final CicCase newCicCase = CicCase.builder()
            .partiesCIC(newParties)
            .fullName(TEST_FIRST_NAME)
            .applicantFullName(APPLICANT_FIRST_NAME)
            .representativeFullName(TEST_FIRST_NAME)
            .build();
        afterData.setCicCase(newCicCase);
        afterData.setCaseFlags(Flags.builder().build());
        afterData.setSubjectFlags(Flags.builder().build());
        afterData.setApplicantFlags(Flags.builder().build());
        afterData.setRepresentativeFlags(Flags.builder().build());

        beforeData.setCicCase(beforeCicCase);
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

        //Then
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getCaseFlags()).isNotNull();
        assertThat(response.getData().getSubjectFlags()).isNotNull();
        assertThat(response.getData().getApplicantFlags()).isNull();
        assertThat(response.getData().getRepresentativeFlags()).isNotNull();
    }

    @Test
    void shouldSuccessfullyInitialiseFlagsWithNullRepresentativeAndApplicant() {
        //Given
        final CaseData afterData = caseData();
        final CaseData beforeData = caseData();

        final CicCase beforeCicCase = CicCase.builder()
            .build();
        Set<PartiesCIC> newParties = new HashSet<>();
        newParties.add(PartiesCIC.SUBJECT);
        newParties.add(PartiesCIC.APPLICANT);
        newParties.add(PartiesCIC.REPRESENTATIVE);
        final CicCase newCicCase = CicCase.builder()
            .partiesCIC(newParties)
            .fullName(TEST_FIRST_NAME)
            .applicantFullName(APPLICANT_FIRST_NAME)
            .representativeFullName(TEST_FIRST_NAME)
            .build();
        afterData.setCicCase(newCicCase);
        afterData.setSubjectFlags(Flags.builder().build());
        afterData.setApplicantFlags(null);
        afterData.setRepresentativeFlags(null);

        beforeData.setCicCase(beforeCicCase);
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

        //Then
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getCaseFlags()).isNotNull();
        assertThat(response.getData().getSubjectFlags()).isNotNull();
        assertThat(response.getData().getApplicantFlags()).isNotNull();
        assertThat(response.getData().getRepresentativeFlags()).isNotNull();
    }

    @Test
    void shouldSuccessfullyInitialiseFlagsWithOnlySubject() {
        //Given
        final CaseData afterData = caseData();
        final CaseData beforeData = caseData();

        final CicCase beforeCicCase = CicCase.builder()
            .build();
        Set<PartiesCIC> newParties = new HashSet<>();
        newParties.add(PartiesCIC.SUBJECT);
        final CicCase newCicCase = CicCase.builder()
            .partiesCIC(newParties)
            .fullName(TEST_FIRST_NAME)
            .applicantFullName(APPLICANT_FIRST_NAME)
            .representativeFullName(TEST_FIRST_NAME)
            .build();
        afterData.setCicCase(newCicCase);
        afterData.setSubjectFlags(Flags.builder().build());
        afterData.setApplicantFlags(Flags.builder().build());
        afterData.setRepresentativeFlags(Flags.builder().build());

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

        //Then
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getCaseFlags()).isNotNull();
        assertThat(response.getData().getSubjectFlags()).isNotNull();
        assertThat(response.getData().getApplicantFlags()).isNull();
        assertThat(response.getData().getRepresentativeFlags()).isNull();
    }

    @Test
    void shouldSuccessfullyInitialiseFlagsWithNullSubjectFlags() {
        //Given
        final CaseData afterData = caseData();
        final CaseData beforeData = caseData();

        final CicCase beforeCicCase = CicCase.builder()
            .build();
        Set<PartiesCIC> newParties = new HashSet<>();
        newParties.add(PartiesCIC.SUBJECT);
        final CicCase newCicCase = CicCase.builder()
            .partiesCIC(newParties)
            .fullName(TEST_FIRST_NAME)
            .applicantFullName(APPLICANT_FIRST_NAME)
            .representativeFullName(TEST_FIRST_NAME)
            .build();
        afterData.setCicCase(newCicCase);
        afterData.setSubjectFlags(null);
        afterData.setApplicantFlags(Flags.builder().build());
        afterData.setRepresentativeFlags(Flags.builder().build());

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

        //Then
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getCaseFlags()).isNotNull();
        assertThat(response.getData().getSubjectFlags()).isNotNull();
        assertThat(response.getData().getApplicantFlags()).isNull();
        assertThat(response.getData().getRepresentativeFlags()).isNull();
    }

    @Test
    void shouldSuccessfullyInitialiseFlagsWithSubjectFlagsPresent() {
        //Given
        final CaseData afterData = caseData();
        final CaseData beforeData = caseData();

        final CicCase beforeCicCase = CicCase.builder()
            .build();
        Set<PartiesCIC> newParties = new HashSet<>();
        newParties.add(PartiesCIC.SUBJECT);
        final CicCase newCicCase = CicCase.builder()
            .partiesCIC(newParties)
            .fullName(TEST_FIRST_NAME)
            .applicantFullName(APPLICANT_FIRST_NAME)
            .representativeFullName(TEST_FIRST_NAME)
            .build();
        afterData.setCicCase(newCicCase);
        afterData.setSubjectFlags(Flags.builder().build());
        afterData.setApplicantFlags(Flags.builder().build());
        afterData.setRepresentativeFlags(Flags.builder().build());

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

        //Then
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getCaseFlags()).isNotNull();
        assertThat(response.getData().getSubjectFlags()).isNotNull();
        assertThat(response.getData().getApplicantFlags()).isNull();
        assertThat(response.getData().getRepresentativeFlags()).isNull();
    }
}
