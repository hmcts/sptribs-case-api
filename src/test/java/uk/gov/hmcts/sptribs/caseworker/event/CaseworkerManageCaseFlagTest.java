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
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.ManageFlagShowList;
import uk.gov.hmcts.sptribs.caseworker.service.FlagService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.model.Status;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.APPLICANT_FLAG;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASE_FLAG;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.REPRESENTATIVE_FLAG;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.SUBJECT_FLAG;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getApplicantFlags;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCaseFlags;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getRepresentativeFlags;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getSubjectFlags;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_MANAGE_CASE_FLAG;

@ExtendWith(MockitoExtension.class)
class CaseworkerManageCaseFlagTest {

    @InjectMocks
    private CaseworkerManageCaseFlag caseworkerManageCaseFlag;

    @InjectMocks
    private ManageFlagShowList manageFlagShowList;

    @Mock
    FlagService flagService;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        caseworkerManageCaseFlag.setCaseFlagsEnabled(true);
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseworkerManageCaseFlag.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_MANAGE_CASE_FLAG);
    }

    @Test
    void shouldNotConfigureManageCaseFlagIfFeatureFlagFalse() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseworkerManageCaseFlag.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .doesNotContain(CASEWORKER_MANAGE_CASE_FLAG);
    }

    @Test
    void shouldRunAboutToStart() {
        //Given
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();

        CicCase cicCase = CicCase.builder()
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .subjectFlags(getSubjectFlags())
            .representativeFlags(getRepresentativeFlags())
            .applicantFlags(getApplicantFlags())
            .caseFlags(getCaseFlags())
            .build();
        updatedCaseDetails.setData(caseData);
        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerManageCaseFlag.aboutToStart(updatedCaseDetails);

        //Then
        assertThat(response).isNotNull();
    }

    @Test
    void shouldSuccessfullySelectCaseFlag() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        caseData.setCaseFlags(getCaseFlags());
        final CicCase cicCase = CicCase.builder()
            .manageDynamicList(getCaseFlagDynamicList())
            .build();
        caseData.setCicCase(cicCase);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = manageFlagShowList.midEvent(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response).isNotNull();
        assertThat(response.getData().getCicCase().getStatus()).isNotNull();

    }

    @Test
    void shouldSuccessfullySelectApplicantFlag() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        caseData.setApplicantFlags(getApplicantFlags());
        final CicCase cicCase = CicCase.builder()
            .manageDynamicList(getApplicantFlagDynamicList())
            .build();
        caseData.setCicCase(cicCase);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = manageFlagShowList.midEvent(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response).isNotNull();
        assertThat(response.getData().getCicCase().getStatus()).isNotNull();

    }

    @Test
    void shouldSuccessfullySelectRepresentativeFlag() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        caseData.setRepresentativeFlags(getRepresentativeFlags());
        final CicCase cicCase = CicCase.builder()
            .manageDynamicList(getRepresentativeFlagDynamicList())
            .build();
        caseData.setCicCase(cicCase);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = manageFlagShowList.midEvent(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response).isNotNull();
        assertThat(response.getData().getCicCase().getStatus()).isNotNull();

    }

    @Test
    void shouldSuccessfullySelectSubjectFlag() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        caseData.setSubjectFlags(getSubjectFlags());
        final CicCase cicCase = CicCase.builder()
            .manageDynamicList(getSubjectFlagDynamicList())
            .build();
        caseData.setCicCase(cicCase);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = manageFlagShowList.midEvent(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response).isNotNull();
        assertThat(response.getData().getCicCase().getStatus()).isNotNull();
    }

    @Test
    void shouldSuccessfullySelectCaseFlagSubmit() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        caseData.setCaseFlags(getCaseFlags());
        final CicCase cicCase = CicCase.builder()
            .manageDynamicList(getCaseFlagDynamicList())
            .additionalDetail("update")
            .build();
        caseData.setCicCase(cicCase);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> midResponse = manageFlagShowList.midEvent(updatedCaseDetails, beforeDetails);
        assertThat(midResponse.getData().getCicCase().getStatus()).isNotNull();

        cicCase.setStatus(Status.INACTIVE);
        caseData.setCicCase(cicCase);
        updatedCaseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerManageCaseFlag.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse submittedResponse =
            caseworkerManageCaseFlag.submitted(updatedCaseDetails, beforeDetails);
        //Then
        assertThat(response).isNotNull();
        assertThat(submittedResponse).isNotNull();
        assertThat(response.getData().getCaseFlags().getDetails().get(0).getValue().getStatus())
            .isEqualTo(Status.INACTIVE.getLabel());

    }

    @Test
    void shouldSuccessfullySelectApplicantFlagSubmit() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        caseData.setApplicantFlags(getApplicantFlags());
        final CicCase cicCase = CicCase.builder()
            .manageDynamicList(getApplicantFlagDynamicList())
            .build();
        caseData.setCicCase(cicCase);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> midEventResponse = manageFlagShowList.midEvent(updatedCaseDetails, beforeDetails);
        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerManageCaseFlag.aboutToSubmit(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response).isNotNull();
        assertThat(midEventResponse.getData().getCicCase().getStatus()).isNotNull();

    }

    @Test
    void shouldSuccessfullySelectRepresentativeFlagSubmit() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        caseData.setRepresentativeFlags(getRepresentativeFlags());
        final CicCase cicCase = CicCase.builder()
            .manageDynamicList(getRepresentativeFlagDynamicList())
            .build();
        caseData.setCicCase(cicCase);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> midResponse = manageFlagShowList.midEvent(updatedCaseDetails, beforeDetails);
        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerManageCaseFlag.aboutToSubmit(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response).isNotNull();
        assertThat(midResponse.getData().getCicCase().getStatus()).isNotNull();

    }

    @Test
    void shouldSuccessfullySelectSubjectFlagSubmit() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        caseData.setSubjectFlags(getSubjectFlags());
        final CicCase cicCase = CicCase.builder()
            .manageDynamicList(getSubjectFlagDynamicList())
            .build();
        caseData.setCicCase(cicCase);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> midResponse = manageFlagShowList.midEvent(updatedCaseDetails, beforeDetails);
        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerManageCaseFlag.aboutToSubmit(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response).isNotNull();
        assertThat(midResponse.getData().getCicCase().getStatus()).isNotNull();

    }

    private DynamicList getCaseFlagDynamicList() {
        final DynamicListElement listItem = DynamicListElement
            .builder()
            .label(CASE_FLAG + "-0-0")
            .code(UUID.randomUUID())
            .build();
        return DynamicList
            .builder()
            .value(listItem)
            .listItems(List.of(listItem))
            .build();
    }

    private DynamicList getRepresentativeFlagDynamicList() {
        final DynamicListElement listItem = DynamicListElement
            .builder()
            .label(REPRESENTATIVE_FLAG + "-0-0")
            .code(UUID.randomUUID())
            .build();
        return DynamicList
            .builder()
            .value(listItem)
            .listItems(List.of(listItem))
            .build();
    }

    private DynamicList getSubjectFlagDynamicList() {
        final DynamicListElement listItem = DynamicListElement
            .builder()
            .label(SUBJECT_FLAG + "-0-0")
            .code(UUID.randomUUID())
            .build();
        return DynamicList
            .builder()
            .value(listItem)
            .listItems(List.of(listItem))
            .build();
    }

    private DynamicList getApplicantFlagDynamicList() {
        final DynamicListElement listItem = DynamicListElement
            .builder()
            .label(APPLICANT_FLAG + "-0-0")
            .code(UUID.randomUUID())
            .build();
        return DynamicList
            .builder()
            .value(listItem)
            .listItems(List.of(listItem))
            .build();
    }
}
