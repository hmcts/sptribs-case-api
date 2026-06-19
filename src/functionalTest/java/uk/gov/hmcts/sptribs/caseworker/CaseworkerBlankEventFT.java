package uk.gov.hmcts.sptribs.caseworker;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;

@SpringBootTest
public class CaseworkerBlankEventFT extends FunctionalTestSuite {

    @Test
    public void shouldReceiveValidResponseWhenSubmittedCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(
            "classpath:request/casedata/ccd-callback-casedata-caseworker-edit-case-request.json"
        );

        final Response response = triggerCallback(caseData, "blankEvent", SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath("$.confirmation_header")
            .isString()
            .contains("# Blank event finished");
    }
}
