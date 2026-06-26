package uk.gov.hmcts.sptribs.caseworker.event;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.config.WebMvcConfig;
import uk.gov.hmcts.sptribs.document.bundling.model.Bundle;
import uk.gov.hmcts.sptribs.document.bundling.model.BundleIdAndTimestamp;
import uk.gov.hmcts.sptribs.testutil.IdamWireMock;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.REMOVE_BUNDLES;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.REMOVE_BUNDLES_MID_EVENT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {IdamWireMock.PropertiesInitializer.class})
public class CaseworkerRemoveBundlesIT {

    private static final String CASEWORKER_REMOVE_BUNDLES_ABOUT_TO_START_RESPONSE =
        "classpath:responses/caseworker-remove-bundles-about-to-start-response.json";

    private static final String CASEWORKER_REMOVE_BUNDLES_ABOUT_TO_SUBMIT_RESPONSE =
        "classpath:responses/caseworker-remove-bundles-about-to-submit-response.json";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WebMvcConfig webMvcConfig;

    private static final TypeReference<HashMap<String, Object>> TYPE_REFERENCE = new TypeReference<>() {
    };

    @BeforeAll
    static void setUp() {
        IdamWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        IdamWireMock.stopAndReset();
    }

    @Test
    void shouldRemoveAllBundlesAndBundleDataForAllBundlesSelectedForDeletionOnAboutToSubmit() throws Exception {

        String bundleUUID1 = UUID.randomUUID().toString();
        String bundleUUID2 = UUID.randomUUID().toString();
        String bundleUUID3 = UUID.randomUUID().toString();

        final LocalDateTime bundleTimestamp1 = LocalDateTime.now();
        final LocalDateTime bundleTimestamp2 = LocalDateTime.now().minusDays(1);
        final LocalDateTime bundleTimestamp3 = LocalDateTime.now().minusDays(2);

        Document stitchedDocument1 = Document.builder()
            .filename("1-cicBundle.pdf")
            .build();
        Document stitchedDocument2 = Document.builder()
            .filename("2-cicBundle.pdf")
            .build();
        Document stitchedDocument3 = Document.builder()
            .filename("3-cicBundle.pdf")
            .build();

        final CaseData caseData = caseData();
        List<ListValue<Bundle>> existingBundles = new ArrayList<>();
        existingBundles.add(ListValue.<Bundle>builder()
            .id("1")
            .value(Bundle.builder()
                .id(bundleUUID1)
                .dateAndTime(bundleTimestamp1)
                .stitchedDocument(stitchedDocument1)
                .build())
            .build());
        existingBundles.add(ListValue.<Bundle>builder()
            .id("2")
            .value(Bundle.builder()
                .id(bundleUUID2)
                .dateAndTime(bundleTimestamp2)
                .stitchedDocument(stitchedDocument2)
                .build())
            .build());
        existingBundles.add(ListValue.<Bundle>builder()
            .id("3")
            .value(Bundle.builder()
                .id(bundleUUID3)
                .dateAndTime(bundleTimestamp3)
                .stitchedDocument(stitchedDocument3)
                .build())
            .build());
        caseData.setCaseBundles(existingBundles);

        List<ListValue<BundleIdAndTimestamp>> bundleIdAndTimestamps = List.of(
            ListValue.<BundleIdAndTimestamp>builder()
            .id("1")
            .value(BundleIdAndTimestamp.builder()
                .bundleId(bundleUUID1)
                .dateAndTime(bundleTimestamp1)
                .build())
            .build(),
            ListValue.<BundleIdAndTimestamp>builder()
                .id("2")
                .value(BundleIdAndTimestamp.builder()
                    .bundleId(bundleUUID2)
                    .dateAndTime(bundleTimestamp2)
                    .build())
                .build(),
            ListValue.<BundleIdAndTimestamp>builder()
                .id("3")
                .value(BundleIdAndTimestamp.builder()
                    .bundleId(bundleUUID3)
                    .dateAndTime(bundleTimestamp3)
                    .build())
                .build()
        );
        caseData.setCaseBundleIdsAndTimestamps(bundleIdAndTimestamps);

        String aboutToStartResponse = mockMvc.perform(post(ABOUT_TO_START_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, REMOVE_BUNDLES)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(aboutToStartResponse)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(CASEWORKER_REMOVE_BUNDLES_ABOUT_TO_START_RESPONSE)));

        HashMap<String, Object> aboutToStartResponseMap = objectMapper.readValue(aboutToStartResponse, TYPE_REFERENCE);
        CaseData caseDataAfterAboutToStart = objectMapper.convertValue(aboutToStartResponseMap.get("data"), CaseData.class);

        List<DynamicListElement> removeBundlesLabels = caseDataAfterAboutToStart.getCicCase().getRemoveBundlesList().getListItems();

        assertThat(removeBundlesLabels).hasSize(3);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

        assertThat(removeBundlesLabels.getFirst().getLabel())
            .isEqualTo(bundleTimestamp1.format(formatter) + " -- " + "1-cicBundle.pdf");
        assertThat(removeBundlesLabels.get(1).getLabel())
            .isEqualTo(bundleTimestamp2.format(formatter) + " -- " + "2-cicBundle.pdf");
        assertThat(removeBundlesLabels.get(2).getLabel())
            .isEqualTo(bundleTimestamp3.format(formatter) + " -- " + "3-cicBundle.pdf");


        List<DynamicListElement> value = new ArrayList<>();
        value.add(removeBundlesLabels.getFirst());
        value.add(removeBundlesLabels.get(1));

        caseDataAfterAboutToStart.getCicCase().getRemoveBundlesList().setValue(value);

        String aboutToSubmitResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseDataAfterAboutToStart, REMOVE_BUNDLES)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(aboutToSubmitResponse)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(CASEWORKER_REMOVE_BUNDLES_ABOUT_TO_SUBMIT_RESPONSE)));

        HashMap<String, Object> aboutToSubmitResponseMap = objectMapper.readValue(aboutToSubmitResponse, TYPE_REFERENCE);
        CaseData caseDataAfterAboutToSubmit = objectMapper.convertValue(aboutToSubmitResponseMap.get("data"), CaseData.class);

        assertThat(caseDataAfterAboutToSubmit.getCaseBundles()).hasSize(1);
        assertThat(caseDataAfterAboutToSubmit.getCaseBundles().getFirst().getId()).isEqualTo("1");
        assertThat(caseDataAfterAboutToSubmit.getCaseBundles().getFirst().getValue().getId()).isEqualTo(bundleUUID3);
        assertThat(caseDataAfterAboutToSubmit.getCaseBundleIdsAndTimestamps()).hasSize(1);
        assertThat(caseDataAfterAboutToSubmit.getCaseBundleIdsAndTimestamps().getFirst().getId()).isEqualTo("1");
        assertThat(caseDataAfterAboutToSubmit.getCaseBundleIdsAndTimestamps().getFirst().getValue().getBundleId()).isEqualTo(bundleUUID3);
        assertThat(caseDataAfterAboutToSubmit.getCicCase().getRemoveBundlesList().getValue()).isNull();
    }

    @Test
    void shouldReturnErrorWhenNoBundlesSelectedForDeletionOnMidEvent() throws Exception {

        String bundleUUID1 = UUID.randomUUID().toString();
        String bundleUUID2 = UUID.randomUUID().toString();
        String bundleUUID3 = UUID.randomUUID().toString();

        final LocalDateTime bundleTimestamp1 = LocalDateTime.now();
        final LocalDateTime bundleTimestamp2 = LocalDateTime.now().minusDays(1);
        final LocalDateTime bundleTimestamp3 = LocalDateTime.now().minusDays(2);

        Document stitchedDocument1 = Document.builder()
            .filename("1-cicBundle.pdf")
            .build();
        Document stitchedDocument2 = Document.builder()
            .filename("2-cicBundle.pdf")
            .build();
        Document stitchedDocument3 = Document.builder()
            .filename("3-cicBundle.pdf")
            .build();

        final CaseData caseData = caseData();
        List<ListValue<Bundle>> existingBundles = new ArrayList<>();
        existingBundles.add(ListValue.<Bundle>builder()
            .id("1")
            .value(Bundle.builder()
                .id(bundleUUID1)
                .dateAndTime(bundleTimestamp1)
                .stitchedDocument(stitchedDocument1)
                .build())
            .build());
        existingBundles.add(ListValue.<Bundle>builder()
            .id("2")
            .value(Bundle.builder()
                .id(bundleUUID2)
                .dateAndTime(bundleTimestamp2)
                .stitchedDocument(stitchedDocument2)
                .build())
            .build());
        existingBundles.add(ListValue.<Bundle>builder()
            .id("3")
            .value(Bundle.builder()
                .id(bundleUUID3)
                .dateAndTime(bundleTimestamp3)
                .stitchedDocument(stitchedDocument3)
                .build())
            .build());
        caseData.setCaseBundles(existingBundles);

        List<ListValue<BundleIdAndTimestamp>> bundleIdAndTimestamps = List.of(
            ListValue.<BundleIdAndTimestamp>builder()
                .id("1")
                .value(BundleIdAndTimestamp.builder()
                    .bundleId(bundleUUID1)
                    .dateAndTime(bundleTimestamp1)
                    .build())
                .build(),
            ListValue.<BundleIdAndTimestamp>builder()
                .id("2")
                .value(BundleIdAndTimestamp.builder()
                    .bundleId(bundleUUID2)
                    .dateAndTime(bundleTimestamp2)
                    .build())
                .build(),
            ListValue.<BundleIdAndTimestamp>builder()
                .id("3")
                .value(BundleIdAndTimestamp.builder()
                    .bundleId(bundleUUID3)
                    .dateAndTime(bundleTimestamp3)
                    .build())
                .build()
        );
        caseData.setCaseBundleIdsAndTimestamps(bundleIdAndTimestamps);

        String aboutToStartResponse = mockMvc.perform(post(ABOUT_TO_START_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, REMOVE_BUNDLES)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(aboutToStartResponse)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(CASEWORKER_REMOVE_BUNDLES_ABOUT_TO_START_RESPONSE)));

        HashMap<String, Object> aboutToStartResponseMap = objectMapper.readValue(aboutToStartResponse, TYPE_REFERENCE);
        CaseData caseDataAfterAboutToStart = objectMapper.convertValue(aboutToStartResponseMap.get("data"), CaseData.class);

        List<DynamicListElement> removeBundlesLabels = caseDataAfterAboutToStart.getCicCase().getRemoveBundlesList().getListItems();

        assertThat(removeBundlesLabels).hasSize(3);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

        assertThat(removeBundlesLabels.getFirst().getLabel())
            .isEqualTo(bundleTimestamp1.format(formatter) + " -- " + "1-cicBundle.pdf");
        assertThat(removeBundlesLabels.get(1).getLabel())
            .isEqualTo(bundleTimestamp2.format(formatter) + " -- " + "2-cicBundle.pdf");
        assertThat(removeBundlesLabels.get(2).getLabel())
            .isEqualTo(bundleTimestamp3.format(formatter) + " -- " + "3-cicBundle.pdf");

        caseDataAfterAboutToStart.getCicCase().getRemoveBundlesList().setValue(new ArrayList<>());

        String midEventResponse = mockMvc.perform(post(REMOVE_BUNDLES_MID_EVENT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseDataAfterAboutToStart, REMOVE_BUNDLES)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors").value("Select at least one bundle to remove"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(midEventResponse)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(CASEWORKER_REMOVE_BUNDLES_ABOUT_TO_START_RESPONSE)));

        HashMap<String, Object> midEventResponseMap = objectMapper.readValue(midEventResponse, TYPE_REFERENCE);
        CaseData caseDataAfterAboutToSubmit = objectMapper.convertValue(midEventResponseMap.get("data"), CaseData.class);

        assertThat(caseDataAfterAboutToSubmit.getCicCase().getRemoveBundlesList().getValue()).isEmpty();

    }
}
