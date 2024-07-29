package uk.gov.hmcts.sptribs.dmn;

import io.restassured.http.Headers;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.sptribs.dmn.domain.entities.idam.CredentialRequest;
import uk.gov.hmcts.sptribs.dmn.domain.entities.task.EventCaseData;
import uk.gov.hmcts.sptribs.dmn.domain.entities.task.TestScenario;
import uk.gov.hmcts.sptribs.dmn.service.AuthorizationHeadersProvider;
import uk.gov.hmcts.sptribs.dmn.service.CcdCaseCreator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ScenarioRunner {
    @Autowired
    protected AuthorizationHeadersProvider authorizationHeadersProvider;

    @Autowired
    private CcdCaseCreator ccdCaseCreator;

//    public void processScenario(Scenario scenario) {
//
//    }

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
                addAssignedCaseId(caseValues, caseId, scenario);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
