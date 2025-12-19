package uk.gov.hmcts.sptribs.caseworker;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.sptribs.testutil.FunctionalTestSuite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CREATE_BUNDLE;
import static uk.gov.hmcts.sptribs.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class CaseworkerCreateBundleFT extends FunctionalTestSuite {

    private static final String REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-caseworker-create-bundle-about-to-submit.json";
    private static final String RESPONSE =
        "classpath:responses/response-caseworker-create-bundle-about-to-submit.json";

    @Test
    public void shouldCreateBundleInAboutToSubmitCallback() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST);
        final Response response = triggerCallback(caseData, CREATE_BUNDLE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(RESPONSE)));
    }

    @Test
    public void shouldNotSetTimestampForOldBundlesWithoutTimestampEntryWhenCreatingNewBundle() throws Exception {

        String existingOldBundleUUID = UUID.randomUUID().toString();

        final Map<String, Object> caseDataBefore = caseData(REQUEST);
        Map<String, Object> existingBundle = new HashMap<>();
        existingBundle.put("id", "1");
        Map<String, Object> bundleValue = new HashMap<>();
        bundleValue.put("id", existingOldBundleUUID);
        existingBundle.put("value", bundleValue);

        List<Map<String, Object>> existingBundles = new ArrayList<>();
        existingBundles.add(existingBundle);
        caseDataBefore.put("caseBundles", existingBundles);

        final Map<String, Object> caseData = caseData(REQUEST);
        caseData.put("caseBundleIdsAndTimestamps", new ArrayList<>());

        final Response response = triggerCallback(caseData, caseDataBefore, CREATE_BUNDLE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .inPath("$.errors")
            .isAbsent();
    }

    @Test
    public void shouldHandleNullBundleIdsAndTimestampsGracefully() throws Exception {
        final Map<String, Object> caseData = caseData(REQUEST);
        caseData.remove("caseBundleIdsAndTimestamps");

        final Response response = triggerCallback(caseData, CREATE_BUNDLE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .inPath("$.errors")
            .isAbsent();
    }
}
