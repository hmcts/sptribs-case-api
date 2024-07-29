package uk.gov.hmcts.sptribs.dmn;

import io.restassured.http.Headers;
import uk.gov.hmcts.sptribs.dmn.domain.entities.idam.CredentialRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ScenarioRunner {

    public void processScenario(Scenario scenario) {

    }

    private void createBaseCcdCase(Scenario scenario) throws IOException {
        Map<String, Object> scenarioValues = scenario.getScenarioMapValues();
        CredentialRequest credentialRequest = extractCredentialRequest(scenarioValues, "required.credentials");
        Headers requestAuthorizationHeaders = authorizationHeadersProvider.getWaUserAuthorization(credentialRequest);

        List<Map<String, Object>> ccdCaseToCreate = new ArrayList<>(Objects.requireNonNull(
            MapValueExtractor.extract(scenarioValues, "required.ccd")));



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

    private CredentialRequest extractCredentialRequest(Map<String, Object> map, String path) {
        String credentialsKey = extractOrThrow(map, path + ".key");
        boolean granularPermission = extractOrDefault(map, path + ".granularPermission", false);

        return new CredentialRequest(credentialsKey, granularPermission);
    }
}
