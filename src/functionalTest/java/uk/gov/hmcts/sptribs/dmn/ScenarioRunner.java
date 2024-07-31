package uk.gov.hmcts.sptribs.dmn;

import io.restassured.http.Headers;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.sptribs.dmn.domain.entities.idam.CredentialRequest;
import uk.gov.hmcts.sptribs.dmn.domain.entities.idam.UserInfo;
import uk.gov.hmcts.sptribs.dmn.domain.entities.task.EventCaseData;
import uk.gov.hmcts.sptribs.dmn.domain.entities.task.Expectation;
import uk.gov.hmcts.sptribs.dmn.domain.entities.task.TestScenario;
import uk.gov.hmcts.sptribs.dmn.domain.entities.task.ScenarioTestData;
import uk.gov.hmcts.sptribs.dmn.service.AuthorizationHeadersProvider;
import uk.gov.hmcts.sptribs.dmn.service.CcdCaseCreator;
import uk.gov.hmcts.sptribs.dmn.service.TaskMgmApiRetrieverService;
import uk.gov.hmcts.sptribs.testutil.CaseIdUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.sptribs.dmn.service.AuthorizationHeadersProvider.AUTHORIZATION;

public class ScenarioRunner {
    @Autowired
    protected AuthorizationHeadersProvider authorizationHeadersProvider;

    @Autowired
    private CcdCaseCreator ccdCaseCreator;

    @Autowired
    private TaskMgmApiRetrieverService taskMgmApiRetrievableService;

    public void processScenario(TestScenario scenario) throws Exception {
        createBaseCcdCase(scenario);

        // values -> ScenarioTestData
        ScenarioTestData test = scenario.getTest();
        processTestRequest(test, scenario);

        // TODO - All json files use taskRetrievalApi - should only cover that condition?
        // TODO - does expectations need to be a list
        List<Expectation> expectations = test.getExpectations();

        for(Expectation expectation : expectations) {
            int expectedTasks = expectation.getNumberOfTasksAvailable();
            // TODO - no numberOfMessagesToCheck?
            List<String> expectationCaseIds = CaseIdUtil.extractAllAssignedCaseIdOrDefault(expectation, scenario);

            verifyTasks(scenario, "taskRetrievalApi", expectation, expectedTasks, expectationCaseIds);
            // TODO is verifyMessages necessary?


        }



    }

    private void processTestRequest(ScenarioTestData values, TestScenario scenario) throws Exception {

        TestRequestType requestType = TestRequestType.valueOf(MapValueExtractor.extractOrDefault(
                request, "type", "MESSAGE")); // TODO - (request) type is not in the json values - should use default value?

        CredentialRequest credentialRequest = new CredentialRequest(scenario.getRequiredCredentials(), false);
        Headers requestAuthorizationHeaders = authorizationHeadersProvider.getWaUserAuthorization(credentialRequest);

        String userToken = requestAuthorizationHeaders.getValue(AUTHORIZATION);
        UserInfo userInfo = authorizationHeadersProvider.getUserInfo(userToken);

        log.info("{} request", requestType);
        // TODO - come back to this - do we only need Message case - when would we need the others? - could be based on other dmns e.g. completion?
        switch (requestType) {
            case MESSAGE:
                messageInjector.injectMessage(
                        request,
                        scenario,
                        userInfo
                );
                break;
            case CLAIM:
                taskManagementService.claimTask(scenario, requestAuthorizationHeaders, userInfo);
                break;
            case ASSIGN:
                UserInfo assignee = getAssigneeInfo(request);
                taskManagementService.assignTask(scenario, requestAuthorizationHeaders, assignee);
                break;
            case COMPLETE:
                taskManagementService.completeTask(scenario, requestAuthorizationHeaders, userInfo);
                break;
            default:
                throw new Exception("Invalid request type [" + requestType + "]");
        }
    }

    private void createBaseCcdCase(TestScenario scenario) throws IOException {
        CredentialRequest credentialRequest = new CredentialRequest(scenario.getRequiredCredentials(), false);
        Headers requestAuthorizationHeaders = authorizationHeadersProvider.getWaUserAuthorization(credentialRequest);

        List<EventCaseData> ccdCaseToCreate = scenario.getRequiredCaseData();

        ccdCaseToCreate.forEach(caseValues -> {
            try {
                String caseId = ccdCaseCreator.createCase(
                    caseValues,
                    scenario.getJurisdiction(),
                    scenario.getCaseType(),
                    requestAuthorizationHeaders
                );
                scenario.addAssignedCaseId("defaultCaseId", caseId);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void verifyTasks(TestScenario scenario, String taskRetrieverOption, Expectation expectation,
                             int expectedTasks, List<String> expectationCaseIds) throws IOException {
        if (expectedTasks > 0) {
            CredentialRequest credentialRequest = new CredentialRequest(scenario.getRequiredCredentials(), false);
            Headers expectationAuthorizationHeaders =
                    authorizationHeadersProvider.getWaUserAuthorization(credentialRequest);

            // TODO - camunda api call necessary?
            if (TaskRetrieverEnum.CAMUNDA_API.getId().equals(taskRetrieverOption)) {
                camundaTaskRetrievableService.retrieveTask(
                        expectationValue,
                        scenario,
                        expectationCaseIds.get(0),
                        expectationAuthorizationHeaders
                );
            } else {
                taskMgmApiRetrievableService.retrieveTask(
                        expectation,
                        scenario,
                        expectationCaseIds,
                        expectationAuthorizationHeaders
                );
            }
        }
    }

    // TODO - work out if needing non default case id is needed
//    public static void addAssignedCaseId(EventCaseData values, String caseId, TestScenario scenario) {
//        String assignedCaseIdKey = extract(values, ASSIGNED_CASE_ID_KEY_FIELD);
//        if (StringUtils.isNotEmpty(assignedCaseIdKey)) {
//            scenario.addAssignedCaseId(assignedCaseIdKey, caseId);
//        } else {
//            scenario.addAssignedCaseId(DEFAULT_ASSIGNED_CASE_ID_KEY, caseId);
//        }
//    }
}
