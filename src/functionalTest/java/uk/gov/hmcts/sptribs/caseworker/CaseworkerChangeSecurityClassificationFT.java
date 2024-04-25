package uk.gov.hmcts.sptribs.caseworker;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.sptribs.caseworker.util.EventConstants;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;

@SpringBootTest
public class CaseworkerChangeSecurityClassificationFT extends FunctionalTestSuite {

    private static final String REQUEST = "classpath:request/casedata/ccd-callback-casedata.json";

    private static final String CONFIRMATION_HEADER = "$.confirmation_header";

    @Test
    public void shouldTriggerSuccessfulResponseIfSubmitCallbackIsInvoked() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST);
        final Response response = triggerCallback(caseData, EventConstants.CHANGE_SECURITY_CLASS, SUBMITTED_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath(CONFIRMATION_HEADER)
            .isEqualTo("# Security classification changed");

    }

}
