package uk.gov.hmcts.sptribs.caseworker.event;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.config.WebMvcConfig;
import uk.gov.hmcts.sptribs.recordlisting.model.HearingVenue;
import uk.gov.hmcts.sptribs.recordlisting.model.Region;
import uk.gov.hmcts.sptribs.testutil.IdamWireMock;
import uk.gov.hmcts.sptribs.testutil.RefDataLocationWireMock;

import java.util.List;
import java.util.Set;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_HEARING_OPTIONS;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.COURT_TYPE_ID;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.ReadyToList;
import static uk.gov.hmcts.sptribs.testutil.RefDataLocationWireMock.stubGetHearingVenues;
import static uk.gov.hmcts.sptribs.testutil.RefDataLocationWireMock.stubGetHearingVenuesForFailure;
import static uk.gov.hmcts.sptribs.testutil.RefDataLocationWireMock.stubGetRegions;
import static uk.gov.hmcts.sptribs.testutil.RefDataLocationWireMock.stubGetRegionsForFailure;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.HEARING_OPTIONS_REGION_DATA_MID_EVENT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getMockedHearingVenueData;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {
    IdamWireMock.PropertiesInitializer.class,
    RefDataLocationWireMock.PropertiesInitializer.class
})
public class CaseworkerHearingOptionsIT {

    private static final String CASEWORKER_HEARING_OPTIONS_ABOUT_TO_START_RESPONSE =
        "classpath:responses/caseworker-hearing-options-about-to-start-response.json";

    private static final String CASEWORKER_HEARING_OPTIONS_MID_EVENT_RESPONSE =
        "classpath:responses/caseworker-hearing-options-mid-event-response.json";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WebMvcConfig webMvcConfig;

    @MockitoBean
    private AuthTokenGenerator serviceTokenGenerator;

    @BeforeAll
    static void setUp() {
        IdamWireMock.start();
        RefDataLocationWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        IdamWireMock.stopAndReset();
        RefDataLocationWireMock.stopAndReset();
    }

    @Test
    void shouldTransitionStateToReadyToListWhenPreStateIsCaseManagementAboutToSubmit() throws Exception {
        final CaseData caseData = caseData();
        final DynamicList hearingVenues = getMockedHearingVenueData();
        caseData.getListing().setHearingVenues(hearingVenues);
        caseData.getListing().setVenueNotListedOption(Set.of());
        final CallbackRequest callbackRequest = callbackRequest(caseData, CASEWORKER_HEARING_OPTIONS);
        callbackRequest.getCaseDetails().setState(CaseManagement.getName());

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andExpect(jsonPath("$.state").value("ReadyToList"));
    }

    @Test
    void shouldNotTransitionStateWhenPreStateIsReadyToListAboutToSubmit() throws Exception {
        final CaseData caseData = caseData();
        final DynamicList hearingVenues = getMockedHearingVenueData();
        caseData.getListing().setHearingVenues(hearingVenues);
        caseData.getListing().setVenueNotListedOption(Set.of());
        final CallbackRequest callbackRequest = callbackRequest(caseData, CASEWORKER_HEARING_OPTIONS);
        callbackRequest.getCaseDetails().setState(ReadyToList.getName());

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andExpect(jsonPath("$.state").value("ReadyToList"));
    }

    @Test
    void shouldPopulateRegionDataWhenRegionListIsNullAboutToStart() throws Exception {
        final CaseData caseData = caseData();
        stubGetRegions(getRegionsResponse());

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        String response = mockMvc.perform(post(ABOUT_TO_START_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CASEWORKER_HEARING_OPTIONS)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(CASEWORKER_HEARING_OPTIONS_ABOUT_TO_START_RESPONSE)));
    }

    @Test
    void shouldNotPopulateRegionDataWhenCallToLocationApiUnsuccessfulAboutToStart() throws Exception {
        final CaseData caseData = caseData();
        stubGetRegionsForFailure();

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        String response = mockMvc.perform(post(ABOUT_TO_START_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CASEWORKER_HEARING_OPTIONS)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .inPath("$.data.regionList")
            .isEqualTo("{\"list_items\":[]}");
    }

    @Test
    void shouldPopulateVenueDataWhenVenueListIsNullMidEvent() throws Exception {
        final CaseData caseData = caseData();
        caseData.getListing().setRegionList(
            DynamicList.builder()
                .value(DynamicListElement.builder().label("1-London").build())
                .build());
        stubGetHearingVenues("1", getHearingVenuesResponse());

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        String response = mockMvc.perform(post(HEARING_OPTIONS_REGION_DATA_MID_EVENT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CASEWORKER_HEARING_OPTIONS)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(CASEWORKER_HEARING_OPTIONS_MID_EVENT_RESPONSE)));
    }

    @Test
    void shouldNotPopulateVenueDataWhenCallToLocationApiUnsuccessfulMidEvent() throws Exception {
        final CaseData caseData = caseData();
        caseData.getListing().setRegionList(
            DynamicList.builder()
                .value(DynamicListElement.builder().label("1-London").build())
                .build());
        stubGetHearingVenuesForFailure("1");

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        String response = mockMvc.perform(post(HEARING_OPTIONS_REGION_DATA_MID_EVENT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CASEWORKER_HEARING_OPTIONS)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .inPath("$.data.hearingVenues")
            .isEqualTo("{\"list_items\":[]}");
        assertThatJson(response)
            .inPath("$.data.hearingVenuesMessage")
            .isEqualTo("Unable to retrieve Hearing Venues data");
    }

    private String getHearingVenuesResponse() throws JsonProcessingException {
        HearingVenue venue1 = HearingVenue.builder()
            .regionId("1")
            .venueName("London Court")
            .region("England")
            .courtName("London Court")
            .courtAddress("1 Court Street")
            .courtVenueId("123")
            .courtTypeId(COURT_TYPE_ID)
            .build();
        HearingVenue venue2 = HearingVenue.builder()
            .regionId("1")
            .venueName("Manchester Court")
            .region("England")
            .courtName("Manchester Court")
            .courtAddress("1 Court Avenue")
            .courtVenueId("456")
            .courtTypeId(COURT_TYPE_ID)
            .build();

        List<HearingVenue> venues = List.of(venue1, venue2);

        return objectMapper.writeValueAsString(venues);
    }

    private String getRegionsResponse() throws JsonProcessingException {
        Region venue1 = Region.builder()
            .regionId("1")
            .description("England")
            .build();
        Region venue2 = Region.builder()
            .regionId("2")
            .description("Wales")
            .build();
        Region venue3 = Region.builder()
            .regionId("3")
            .description("Scotland")
            .build();
        Region venue4 = Region.builder()
            .regionId("4")
            .description("Northern Ireland")
            .build();

        List<Region> regions = List.of(venue1, venue2, venue3, venue4);

        return objectMapper.writeValueAsString(regions);
    }
}
