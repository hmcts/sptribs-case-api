package uk.gov.hmcts.sptribs.systemupdate.event;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemTriggerStitchCollateHearingBundle.SYSTEM_TRIGGER_STITCH_COLLATE_HEARING_BUNDLE;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;

@SpringBootTest
public class SystemTriggerStitchCollateHearingBundleFT extends FunctionalTestSuite {

    private static final String REQUEST = "classpath:request/casedata/ccd-callback-casedata-general.json";

    @Test
    public void shouldSetStitchHearingBundleTaskToYesInAboutToSubmit() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST);
        final Response response = triggerCallback(caseData, SYSTEM_TRIGGER_STITCH_COLLATE_HEARING_BUNDLE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .inPath("$.data.stitchHearingBundleTask")
            .isEqualTo("Yes");
    }
}
