package uk.gov.hmcts.sptribs.systemupdate.event;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemMigrateCaseFlags.SYSTEM_MIGRATE_CASE_FLAGS;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class SystemMigrateCaseFlagsFT extends FunctionalTestSuite {

    private static final String REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-system-migrate-case-flags-about-to-submit.json";
    private static final String RESPONSE =
        "classpath:responses/response-system-migrate-case-flags-about-to-submit.json";

    @Test
    public void shouldMigrateCaseFlagsOnAboutToSubmit() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST);
        final Response response = triggerCallback(caseData, SYSTEM_MIGRATE_CASE_FLAGS, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(RESPONSE)));
    }
}
