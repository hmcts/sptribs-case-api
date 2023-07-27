package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.FlagAdditionalInfo;
import uk.gov.hmcts.sptribs.caseworker.model.FlagLevel;
import uk.gov.hmcts.sptribs.caseworker.model.FlagType;
import uk.gov.hmcts.sptribs.ciccase.model.ApplicantCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

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

    @InjectMocks
    private FlagAdditionalInfo flagAdditionalInfo;

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
        cicCase.setFlagType(FlagType.OTHER);
        cicCase.setFlagAdditionalDetail("some detail");
        cicCase.setFlagLevel(FlagLevel.PARTY_LEVEL);
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
        assertThat(response.getData().getRepresentativeFlags()).isNotNull();
        assertThat(submittedCallbackResponse).isNotNull();

        updatedCaseDetails.setData(caseData);
        AboutToStartOrSubmitResponse<CaseData, State> response2 =
            caseworkerCaseFlag.aboutToSubmit(updatedCaseDetails, beforeDetails);
        assertThat(response2.getData().getRepresentativeFlags()).isNotNull();
    }

    @Test
    void shouldSuccessfullyApplicantAddFlag() {
        //Given
        final CaseData caseData = caseData();
        caseData.setNote("This is a test note");
        final CicCase cicCase = new CicCase();
        cicCase.setFlagType(FlagType.OTHER);
        cicCase.setFlagAdditionalDetail("some detail");
        cicCase.setFlagLevel(FlagLevel.PARTY_LEVEL);
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
        assertThat(response.getData().getApplicantFlags()).isNotNull();
        assertThat(submittedCallbackResponse).isNotNull();

        updatedCaseDetails.setData(caseData);
        AboutToStartOrSubmitResponse<CaseData, State> response2 =
            caseworkerCaseFlag.aboutToSubmit(updatedCaseDetails, beforeDetails);
        assertThat(response2.getData().getApplicantFlags()).isNotNull();
    }

    @Test
    void shouldSuccessfullyAddFlagSubject() {
        //Given
        final CaseData caseData = caseData();
        caseData.setNote("This is a test note");
        final CicCase cicCase = new CicCase();
        cicCase.setFlagType(FlagType.OTHER);
        cicCase.setFlagLevel(FlagLevel.PARTY_LEVEL);
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
        assertThat(response.getData().getSubjectFlags()).isNotNull();
        assertThat(submittedCallbackResponse).isNotNull();

        updatedCaseDetails.setData(caseData);
        AboutToStartOrSubmitResponse<CaseData, State> response2 =
            caseworkerCaseFlag.aboutToSubmit(updatedCaseDetails, beforeDetails);
        assertThat(response2.getData().getSubjectFlags()).isNotNull();
    }

    @Test
    void shouldSuccessfullyAddCaseFlag() {
        //Given
        final CaseData caseData = caseData();
        caseData.setNote("This is a test note");
        CicCase cicCase = new CicCase();
        cicCase.setFlagType(FlagType.OTHER);
        cicCase.setFlagAdditionalDetail("some detail");
        cicCase.setFlagLevel(FlagLevel.CASE_LEVEL);
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
        assertThat(response.getData().getCaseFlags()).isNotNull();
        assertThat(submittedCallbackResponse).isNotNull();
        updatedCaseDetails.setData(caseData);
        AboutToStartOrSubmitResponse<CaseData, State> response2 =
            caseworkerCaseFlag.aboutToSubmit(updatedCaseDetails, beforeDetails);
        assertThat(response2.getData().getCaseFlags()).isNotNull();
    }

    @Test
    void shouldCheckAdditionalInfoLength() {
        //Given
        final CaseData caseData = caseData();
        caseData.setNote("This is a test note");
        CicCase cicCase = new CicCase();
        cicCase.setFlagType(FlagType.OTHER);
        cicCase.setFlagAdditionalDetail("some detailsome detaisome detaisome detaisome detaisome detaisome detai"
            + "some detaisome detaisome detaisome detaisome detailsome detaisome detaisome detaisome detaisome"
            + " detailsome detaisome detaisome detaisome detaisome detai");
        cicCase.setFlagLevel(FlagLevel.CASE_LEVEL);
        caseData.setCicCase(cicCase);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            flagAdditionalInfo.midEvent(updatedCaseDetails, beforeDetails);


        //Then
        assertThat(response.getErrors()).hasSize(1);
    }
}
