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
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.caseworker.model.SecurityClass;
import uk.gov.hmcts.sptribs.caseworker.service.ExtendedCaseDataService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.testutil.TestDataHelper;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CHANGE_SECURITY_CLASS;

@ExtendWith(MockitoExtension.class)
class CaseWorkerChangeSecurityClassificationTest {
    @InjectMocks
    private CaseworkerChangeSecurityClassification caseworkerChangeSecurityClassification;

    @Mock
    private HttpServletRequest request;

    @Mock
    private IdamService idamService;

    @Mock
    private ExtendedCaseDataService caseDataService;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseworkerChangeSecurityClassification.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CHANGE_SECURITY_CLASS);
    }

    @Test
    void shouldAdd2LinksToCase() {
        //Given

        CaseData caseData = caseData();
        caseData.setSecurityClass(SecurityClass.PRIVATE);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        //When
        AboutToStartOrSubmitResponse<CaseData, State> response1 =
            caseworkerChangeSecurityClassification.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse submitted = caseworkerChangeSecurityClassification.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response1).isNotNull();
        assertThat(submitted).isNotNull();
    }

    @Test
    void shouldCheckRolesSuccessfully() {
        //Given

        CaseData caseData = caseData();
        caseData.setSecurityClass(SecurityClass.PRIVATE);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        User user = TestDataHelper.getUserWithSeniorJudge();
        when(request.getHeader(any())).thenReturn("listing");
        when(idamService.retrieveUser(any())).thenReturn(user);
        //When
        AboutToStartOrSubmitResponse<CaseData, State> response1 =
            caseworkerChangeSecurityClassification.midEvent(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response1.getErrors()).hasSize(0);
    }

    @Test
    void shouldFailIfInsufficientRolesForSecurityClass() {
        //Given

        CaseData caseData = caseData();
        caseData.setSecurityClass(SecurityClass.PRIVATE);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        User user = TestDataHelper.getUser();
        when(request.getHeader(any())).thenReturn("listing");
        when(idamService.retrieveUser(any())).thenReturn(user);
        //When
        AboutToStartOrSubmitResponse<CaseData, State> response1 =
            caseworkerChangeSecurityClassification.midEvent(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response1.getErrors()).hasSize(1);
    }
}
