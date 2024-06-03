package uk.gov.hmcts.sptribs.systemupdate.event;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemTriggerCompleteHearingOutcome.SYSTEM_TRIGGER_COMPLETE_HEARING_OUTCOME;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;

@SpringBootTest
public class SystemTriggerCompleteHearingOutcomeFT extends FunctionalTestSuite {
    private static final String REQUEST = "classpath:request/casedata/ccd-callback-casedata-general.json";
    private static final String CONFIRMATION_HEADER = "$.confirmation_header";

    @Test
    public void shouldTriggerSubmittedCallback() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST);
        final Response response = triggerCallback(caseData, SYSTEM_TRIGGER_COMPLETE_HEARING_OUTCOME, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isEqualTo("# CompleteHearingOutcome WA Task Triggered");
    }
}
