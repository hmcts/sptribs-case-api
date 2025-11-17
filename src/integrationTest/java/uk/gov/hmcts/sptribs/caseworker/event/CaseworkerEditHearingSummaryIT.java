package uk.gov.hmcts.sptribs.caseworker.event;

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
import uk.gov.hmcts.sptribs.caseworker.model.HearingSummary;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.config.WebMvcConfig;
import uk.gov.hmcts.sptribs.testutil.IdamWireMock;

import static java.util.Collections.emptySet;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.sptribs.ciccase.model.HearingFormat.FACE_TO_FACE;
import static uk.gov.hmcts.sptribs.testutil.IdamWireMock.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.testutil.IdamWireMock.stubForIdamDetails;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASEWORKER_USER_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getDynamicList;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getMembers;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getMockedHearingVenueData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getMockedRegionData;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_EDIT_HEARING_SUMMARY;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {IdamWireMock.PropertiesInitializer.class})
public class CaseworkerEditHearingSummaryIT {
    private static final String CASEWORKER_EDIT_HEARING_SUMMARY_RESPONSE =
        "classpath:responses/caseworker-edit-hearing-summary-response.json";

    private static final String CASEWORKER_EDIT_HEARING_SUMMARY_ABOUT_TO_START_RESPONSE =
        "classpath:responses/caseworker-edit-hearing-summary-about-to-start-response.json";
    private static final String CONFIRMATION_HEADER = "$.confirmation_header";

    @BeforeAll
    static void setUp() {
        IdamWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        IdamWireMock.stopAndReset();
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WebMvcConfig webMvcConfig;

    @Test
    void shouldSuccessfullyEditHearingSummary() throws Exception {
        final CaseData caseData = caseData();
        final HearingSummary hearingSummary = HearingSummary.builder()
            .judge(getDynamicList())
            .memberList(getMembers())
            .build();
        final Listing listing = Listing.builder()
            .regionList(getMockedRegionData())
            .hearingVenues(getMockedHearingVenueData())
            .venueNotListedOption(emptySet())
            .roomAtVenue("G.01")
            .addlInstr("Ground floor")
            .hearingFormat(FACE_TO_FACE)
            .shortNotice(YES)
            .summary(hearingSummary)
            .build();
        caseData.setListing(listing);
        final CicCase cicCase = CicCase.builder()
            .hearingSummaryList(
                DynamicList.builder()
                    .value(
                        DynamicListElement.builder()
                            .label("1 - Final - 21 Apr 2021 10:00")
                            .build()
                    ).build()
            ).build();
        caseData.setCicCase(cicCase);

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, ST_CIC_CASEWORKER);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_EDIT_HEARING_SUMMARY)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andExpect(
                content().json(expectedResponse(CASEWORKER_EDIT_HEARING_SUMMARY_RESPONSE))
            );
    }

    @Test
    void shouldHearingSummaryListInAboutToStart() throws Exception {
        final CaseData caseData = caseData();
        final HearingSummary hearingSummary = HearingSummary.builder()
            .judge(getDynamicList())
            .memberList(getMembers())
            .build();
        final Listing listing = Listing.builder()
            .regionList(getMockedRegionData())
            .hearingVenues(getMockedHearingVenueData())
            .venueNotListedOption(emptySet())
            .roomAtVenue("G.01")
            .addlInstr("Ground floor")
            .hearingFormat(FACE_TO_FACE)
            .shortNotice(YES)
            .summary(hearingSummary)
            .build();
        caseData.setListing(listing);
        final CicCase cicCase = CicCase.builder()
            .hearingSummaryList(
                DynamicList.builder()
                    .value(
                        DynamicListElement.builder()
                            .label("1 - Final - 21 Apr 2021 10:00")
                            .build()
                    ).build()
            ).build();
        caseData.setCicCase(cicCase);

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, ST_CIC_CASEWORKER);

        mockMvc.perform(post(ABOUT_TO_START_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_EDIT_HEARING_SUMMARY)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andExpect(
                content().json(expectedResponse(CASEWORKER_EDIT_HEARING_SUMMARY_ABOUT_TO_START_RESPONSE))
            );
    }

    @Test
    void shouldGenerateSuccessMessageSuccessfullyOnSubmitted() throws Exception {
        final CaseData caseData = caseData();

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, ST_CIC_CASEWORKER);

        String response = mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_EDIT_HEARING_SUMMARY)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("Hearing summary edited");
    }
}
