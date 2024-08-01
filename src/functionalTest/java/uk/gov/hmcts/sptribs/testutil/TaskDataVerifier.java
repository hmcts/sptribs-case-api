package uk.gov.hmcts.sptribs.testutil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.dmn.domain.entities.task.TestScenario;

import java.util.Map;

@Component
public class TaskDataVerifier {

    private final MapFieldAsserter mapFieldAsserter;

    @Autowired
    public TaskDataVerifier(MapFieldAsserter mapFieldAsserter) {
        this.mapFieldAsserter = mapFieldAsserter;
    }

    public void verify(
        TestScenario scenario,
        Map<String, Object> expectedResponse,
        Map<String, Object> actualResponse
    ) {
        String description = scenario.getDescription();

        mapFieldAsserter.assertFields(expectedResponse, actualResponse, (description + ": "));
    }
}
