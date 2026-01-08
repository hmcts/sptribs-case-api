package uk.gov.hmcts.sptribs.caseworker.event;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.config.WebMvcConfig;
import uk.gov.hmcts.sptribs.document.bundling.client.BundleResponse;
import uk.gov.hmcts.sptribs.document.bundling.client.BundlingClient;
import uk.gov.hmcts.sptribs.document.bundling.model.Bundle;
import uk.gov.hmcts.sptribs.document.bundling.model.BundleCallback;
import uk.gov.hmcts.sptribs.testutil.IdamWireMock;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CREATE_BUNDLE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCICDocumentList;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCaseworkerCICDocument;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {IdamWireMock.PropertiesInitializer.class})
public class CaseworkerCreateBundleIT {

    private static final String CASEWORKER_CREATE_BUNDLE_ABOUT_TO_SUBMIT_RESPONSE =
        "classpath:responses/caseworker-create-bundle-about-to-submit-response.json";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WebMvcConfig webMvcConfig;

    @MockitoBean
    private AuthTokenGenerator authTokenGenerator;

    @MockitoBean
    private BundlingClient bundlingClient;

    @MockitoBean
    private Clock clock;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();
    private static final TypeReference<HashMap<String, Object>> TYPE_REFERENCE = new TypeReference<>() {
    };
    private static final TypeReference<List<LinkedHashMap<String, Object>>> LIST_TYPE_REFERENCE = new TypeReference<>() {
    };

    private static final Instant instant = Instant.now();
    private static final ZoneId zoneId = ZoneId.systemDefault();

    @BeforeAll
    static void setUp() {
        IdamWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        IdamWireMock.stopAndReset();
    }

    @BeforeEach
    void setClock() {
        when(clock.instant()).thenReturn(instant);
        when(clock.getZone()).thenReturn(zoneId);
    }

    @Test
    void shouldMakeCallToEvidenceManagementToCreateBundleInAboutToSubmit() throws Exception {
        final CaseData caseData = caseData();
        populateCaseDocuments(caseData);
        final BundleResponse bundleResponse = mock(BundleResponse.class);

        when(bundleResponse.getData()).thenReturn(new LinkedHashMap<>());
        when(bundlingClient.createBundle(
            eq(SERVICE_AUTHORIZATION),
            eq(TEST_AUTHORIZATION_TOKEN),
            any(BundleCallback.class)
        )).thenReturn(bundleResponse);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(caseData, CREATE_BUNDLE)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(CASEWORKER_CREATE_BUNDLE_ABOUT_TO_SUBMIT_RESPONSE)));

        verify(bundlingClient)
            .createBundle(
                eq(SERVICE_AUTHORIZATION),
                eq(TEST_AUTHORIZATION_TOKEN),
                any(BundleCallback.class)
            );
    }

    @Test
    void shouldNotSetTimestampForOldBundlesWithoutTimestampEntryWhenCreatingNewBundle() throws Exception {

        String existingOldBundleUUID1 = UUID.randomUUID().toString();
        String existingOldBundleUUID2 = UUID.randomUUID().toString();

        final CaseData caseDataBefore = caseData();
        List<ListValue<Bundle>> existingBundles = new ArrayList<>();
        existingBundles.add(ListValue.<Bundle>builder()
            .id("1")
            .value(Bundle.builder().id(existingOldBundleUUID1).build())
            .build());
        existingBundles.add(ListValue.<Bundle>builder()
            .id("2")
            .value(Bundle.builder().id(existingOldBundleUUID2).build())
            .build());
        caseDataBefore.setCaseBundles(existingBundles);

        final CaseData caseData = caseData();
        caseData.setCaseBundleIdsAndTimestamps(new ArrayList<>());
        populateCaseDocuments(caseData);

        List<ListValue<Bundle>> apiReturnedBundles = new ArrayList<>();
        String newBundleUUID = UUID.randomUUID().toString();
        apiReturnedBundles.add(ListValue.<Bundle>builder()
            .id("1")
            .value(Bundle.builder().id(existingOldBundleUUID1).build())
            .build());
        apiReturnedBundles.add(ListValue.<Bundle>builder()
            .id("2")
            .value(Bundle.builder().id(existingOldBundleUUID2).build())
            .build());
        apiReturnedBundles.add(ListValue.<Bundle>builder()
            .id("3")
            .value(Bundle.builder().id(newBundleUUID).build())
            .build());

        final BundleResponse bundleResponse = mock(BundleResponse.class);

        LinkedHashMap<String, Object> responseData = new LinkedHashMap<>();
        responseData.put("caseBundles", OBJECT_MAPPER.convertValue(apiReturnedBundles, LIST_TYPE_REFERENCE));

        when(bundleResponse.getData()).thenReturn(responseData);
        when(bundlingClient.createBundle(
            eq(SERVICE_AUTHORIZATION),
            eq(TEST_AUTHORIZATION_TOKEN),
            any(BundleCallback.class)
        )).thenReturn(bundleResponse);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, caseDataBefore, CREATE_BUNDLE)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        HashMap<String, Object> responseMap = objectMapper.readValue(response, TYPE_REFERENCE);
        @SuppressWarnings("unchecked")
        HashMap<String, Object> data = (HashMap<String, Object>) responseMap.get("data");
        @SuppressWarnings("unchecked")
        List<HashMap<String, Object>> caseBundles = (List<HashMap<String, Object>>) data.get("caseBundles");

        assertThat(caseBundles).hasSize(3);

        @SuppressWarnings("unchecked")
        HashMap<String, Object> firstBundleValue = (HashMap<String, Object>) caseBundles.get(0).get("value");
        assertThat(firstBundleValue.get("id")).isEqualTo(newBundleUUID);
        assertThat(firstBundleValue.get("dateAndTime")).isNotNull();

        List<HashMap<String, Object>> oldBundles = caseBundles.subList(1, caseBundles.size());
        assertThat(oldBundles)
            .extracting(bundle -> {
                @SuppressWarnings("unchecked")
                HashMap<String, Object> value = (HashMap<String, Object>) bundle.get("value");
                return value.get("id");
            })
            .containsExactlyInAnyOrder(existingOldBundleUUID1, existingOldBundleUUID2);
        assertThat(oldBundles)
            .extracting(bundle -> {
                @SuppressWarnings("unchecked")
                HashMap<String, Object> value = (HashMap<String, Object>) bundle.get("value");
                return value.get("dateAndTime");
            })
            .allMatch(Objects::isNull);

        @SuppressWarnings("unchecked")
        List<HashMap<String, Object>> bundleIdsAndTimestamps =
            (List<HashMap<String, Object>>) data.get("caseBundleIdsAndTimestamps");
        assertThat(bundleIdsAndTimestamps).hasSize(1);
        @SuppressWarnings("unchecked")
        HashMap<String, Object> timestampEntry = (HashMap<String, Object>) bundleIdsAndTimestamps.get(0).get("value");
        assertThat(timestampEntry.get("bundleId")).isEqualTo(newBundleUUID);
    }

    @Test
    void shouldHandleNullBundleIdsAndTimestampsGracefully() throws Exception {
        String newBundleUUID = UUID.randomUUID().toString();

        final CaseData caseData = caseData();
        caseData.setCaseBundleIdsAndTimestamps(null);
        populateCaseDocuments(caseData);

        List<ListValue<Bundle>> apiReturnedBundles = new ArrayList<>();
        apiReturnedBundles.add(ListValue.<Bundle>builder()
            .id("1")
            .value(Bundle.builder().id(newBundleUUID).build())
            .build());

        final BundleResponse bundleResponse = mock(BundleResponse.class);

        LinkedHashMap<String, Object> responseData = new LinkedHashMap<>();
        responseData.put("caseBundles", OBJECT_MAPPER.convertValue(apiReturnedBundles, LIST_TYPE_REFERENCE));

        when(bundleResponse.getData()).thenReturn(responseData);
        when(bundlingClient.createBundle(
            eq(SERVICE_AUTHORIZATION),
            eq(TEST_AUTHORIZATION_TOKEN),
            any(BundleCallback.class)
        )).thenReturn(bundleResponse);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, CREATE_BUNDLE)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        HashMap<String, Object> responseMap = objectMapper.readValue(response, TYPE_REFERENCE);
        @SuppressWarnings("unchecked")
        HashMap<String, Object> data = (HashMap<String, Object>) responseMap.get("data");
        @SuppressWarnings("unchecked")
        List<HashMap<String, Object>> caseBundles = (List<HashMap<String, Object>>) data.get("caseBundles");

        assertThat(caseBundles).hasSize(1);
        @SuppressWarnings("unchecked")
        HashMap<String, Object> bundleValue = (HashMap<String, Object>) caseBundles.get(0).get("value");
        assertThat(bundleValue.get("dateAndTime")).isNotNull();

        @SuppressWarnings("unchecked")
        List<HashMap<String, Object>> bundleIdsAndTimestamps =
            (List<HashMap<String, Object>>) data.get("caseBundleIdsAndTimestamps");
        assertThat(bundleIdsAndTimestamps).hasSize(1);
    }

    private void populateCaseDocuments(final CaseData caseData) {
        final Order order = Order.builder().uploadedFile(getCICDocumentList("test.pdf")).build();
        final ListValue<Order> orderListValue = new ListValue<>();
        orderListValue.setValue(order);
        caseData.getCicCase().setOrderList(List.of(orderListValue));
        caseData.getCicCase().setApplicantDocumentsUploaded(
            List.of(
                getCaseworkerCICDocument("file1.pdf"),
                getCaseworkerCICDocument("test.pdf"),
                getCaseworkerCICDocument("evidence.pdf")
            )
        );
        caseData.getCicCase().setReinstateDocuments(
            List.of(
                getCaseworkerCICDocument("reinstate_doc.pdf")
            )
        );
    }
}
