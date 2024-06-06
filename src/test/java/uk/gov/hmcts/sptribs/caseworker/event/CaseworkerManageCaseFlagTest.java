package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
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
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_MANAGE_CASE_FLAG;

@ExtendWith(MockitoExtension.class)
class CaseworkerManageCaseFlagTest {

    @InjectMocks
    private CaseworkerManageCaseFlag caseworkerManageCaseFlag;


    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseworkerManageCaseFlag.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_MANAGE_CASE_FLAG);
    }

    @Test
    void shouldSuccessfullySelectApplicantFlagSubmit() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder()
            .build();
        final Set<ApplicantCIC> set = new HashSet<>();
        set.add(ApplicantCIC.APPLICANT_CIC);
        cicCase.setApplicantCIC(set);
        cicCase.setApplicantFullName("Jane Doe");
        cicCase.setNotifyPartyApplicant(set);
        caseData.setCicCase(cicCase);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerManageCaseFlag.aboutToSubmit(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response).isNotNull();
        assertThat(response.getData().getCicCase().getApplicantCIC()).isNotEmpty();
        assertThat(response.getData().getCicCase().getApplicantCIC()).contains(ApplicantCIC.APPLICANT_CIC);

    }

    @Test
    void shouldSuccessfullySelectRepresentativeFlagSubmit() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder()
            .build();
        final Set<RepresentativeCIC> set = new HashSet<>();
        set.add(RepresentativeCIC.REPRESENTATIVE);
        cicCase.setRepresentativeCIC(set);
        cicCase.setRepresentativeFullName("Jane Doe");
        cicCase.setNotifyPartyRepresentative(set);
        caseData.setCicCase(cicCase);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerManageCaseFlag.aboutToSubmit(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response).isNotNull();
        assertThat(response.getData().getCicCase().getRepresentativeCIC()).isNotEmpty();
        assertThat(response.getData().getCicCase().getRepresentativeCIC()).contains(RepresentativeCIC.REPRESENTATIVE);

    }

    @Test
    void shouldSuccessfullySelectSubjectFlagSubmit() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder()
            .build();
        final Set<SubjectCIC> set = new HashSet<>();
        set.add(SubjectCIC.SUBJECT);
        cicCase.setSubjectCIC(set);
        cicCase.setFullName("Jane Doe");
        cicCase.setNotifyPartySubject(set);
        caseData.setCicCase(cicCase);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerManageCaseFlag.aboutToSubmit(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response).isNotNull();
        assertThat(response.getData().getCicCase().getSubjectCIC()).isNotEmpty();
        assertThat(response.getData().getCicCase().getSubjectCIC()).contains(SubjectCIC.SUBJECT);

    }

}
