package uk.gov.hmcts.sptribs.caseworker.event;

import jakarta.servlet.http.HttpServletRequest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CHANGE_SECURITY_CLASS;

@ExtendWith(MockitoExtension.class)
class CaseWorkerChangeSecurityClassTest {
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
        caseworkerChangeSecurityClassification.setSecurityClassificationEnabled(true);
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerChangeSecurityClassification.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CHANGE_SECURITY_CLASS);
    }

    @Test
    void shouldAdd2LinksToCase() {
        final CaseData caseData = caseData();
        caseData.setSecurityClass(SecurityClass.PRIVATE);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerChangeSecurityClassification.aboutToSubmit(updatedCaseDetails, beforeDetails);
        final SubmittedCallbackResponse submitted = caseworkerChangeSecurityClassification.submitted(updatedCaseDetails, beforeDetails);

        assertThat(response).isNotNull();
        assertEquals(response.getSecurityClassification(), SecurityClass.PRIVATE.name());
        assertThat(submitted).isNotNull();
        assertThat(submitted.getConfirmationHeader()).contains("Security classification changed");
    }

    @Test
    void shouldCheckRolesSuccessfully() {
        final CaseData caseData = caseData();
        caseData.setSecurityClass(SecurityClass.PRIVATE);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        final User user = TestDataHelper.getUserWithSeniorJudge();
        when(request.getHeader(any())).thenReturn("listing");
        when(idamService.retrieveUser(any())).thenReturn(user);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerChangeSecurityClassification.midEvent(updatedCaseDetails, beforeDetails);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldFailIfInsufficientRolesForSecurityClass() {
        final CaseData caseData = caseData();
        caseData.setSecurityClass(SecurityClass.PRIVATE);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        final User user = TestDataHelper.getUser();
        when(request.getHeader(any())).thenReturn("listing");
        when(idamService.retrieveUser(any())).thenReturn(user);

        final AboutToStartOrSubmitResponse<CaseData, State> response1 =
            caseworkerChangeSecurityClassification.midEvent(updatedCaseDetails, beforeDetails);

        assertThat(response1.getErrors()).hasSize(1);
        assertThat(response1.getErrors()).contains("You do not have permission to change the case to the selected Security Classification");
    }
}
