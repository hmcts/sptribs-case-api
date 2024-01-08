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
import uk.gov.hmcts.sptribs.ciccase.model.ApplicantCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.service.CcdSupplementaryDataService;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_CASE_FLAG;

@ExtendWith(MockitoExtension.class)
class CaseworkerCaseFlagTest {

    @InjectMocks
    private CaseworkerCaseFlag caseworkerCaseFlag;

    @Mock
    private CcdSupplementaryDataService coreCaseApiService;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        caseworkerCaseFlag.setCaseFlagsEnabled(true);
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseworkerCaseFlag.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_CASE_FLAG);
    }

    @Test
    void shouldNotConfigureCaseFlagIfFeatureFlagFalse() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseworkerCaseFlag.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .doesNotContain(CASEWORKER_CASE_FLAG);
    }

    @Test
    void shouldSuccessfullyAddFlagRepresentative() {
        //Given
        final CaseData caseData = caseData();
        caseData.setNote("This is a test note");

        final CicCase cicCase = new CicCase();
        final Set<RepresentativeCIC> set = new HashSet<>();
        set.add(RepresentativeCIC.REPRESENTATIVE);
        cicCase.setRepresentativeCIC(set);
        cicCase.setRepresentativeFullName("Jane Doe");
        cicCase.setNotifyPartyRepresentative(set);
        caseData.setCicCase(cicCase);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerCaseFlag.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse submittedCallbackResponse = caseworkerCaseFlag.flagCreated(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response).isNotNull();
        assertThat(response.getData().getCicCase().getRepresentativeCIC()).isNotEmpty();
        assertThat(response.getData().getCicCase().getRepresentativeCIC()).contains(RepresentativeCIC.REPRESENTATIVE);

        assertThat(submittedCallbackResponse).isNotNull();
        assertThat(submittedCallbackResponse.getConfirmationHeader()).contains("Flag created");

    }

    @Test
    void shouldSuccessfullyApplicantAddFlag() {
        //Given
        final CaseData caseData = caseData();
        caseData.setNote("This is a test note");
        final CicCase cicCase = new CicCase();
        final Set<ApplicantCIC> set = new HashSet<>();
        set.add(ApplicantCIC.APPLICANT_CIC);
        cicCase.setApplicantCIC(set);
        cicCase.setApplicantFullName("Jane Doe");
        cicCase.setNotifyPartyApplicant(set);
        caseData.setCicCase(cicCase);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerCaseFlag.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse submittedCallbackResponse = caseworkerCaseFlag.flagCreated(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response).isNotNull();
        assertThat(response.getData().getCicCase().getApplicantCIC()).isNotEmpty();
        assertThat(response.getData().getCicCase().getApplicantCIC()).contains(ApplicantCIC.APPLICANT_CIC);

        assertThat(submittedCallbackResponse).isNotNull();
        assertThat(submittedCallbackResponse.getConfirmationHeader()).contains("Flag created");

    }

    @Test
    void shouldSuccessfullyAddFlagSubject() {
        //Given
        final CaseData caseData = caseData();
        caseData.setNote("This is a test note");
        final CicCase cicCase = new CicCase();
        final Set<SubjectCIC> set = new HashSet<>();
        set.add(SubjectCIC.SUBJECT);
        cicCase.setSubjectCIC(set);
        cicCase.setFullName("Jane Doe");
        cicCase.setNotifyPartySubject(set);
        caseData.setCicCase(cicCase);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerCaseFlag.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse submittedCallbackResponse = caseworkerCaseFlag.flagCreated(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response).isNotNull();
        assertThat(response.getData().getCicCase().getSubjectCIC()).isNotEmpty();
        assertThat(response.getData().getCicCase().getSubjectCIC()).contains(SubjectCIC.SUBJECT);

        assertThat(submittedCallbackResponse).isNotNull();
        assertThat(submittedCallbackResponse.getConfirmationHeader()).contains("Flag created");

    }

}
