package uk.gov.hmcts.sptribs.dmn.service;

import io.restassured.http.Headers;
import uk.gov.hmcts.sptribs.dmn.domain.entities.task.Expectation;
import uk.gov.hmcts.sptribs.dmn.domain.entities.task.TestScenario;

import java.util.Map;

public interface TaskRetrieverService {

    void retrieveTask(Expectation expectation,
                      TestScenario scenario,
                      String caseId,
                      Headers authorizationHeaders);
}
