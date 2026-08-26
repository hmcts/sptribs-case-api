package uk.gov.hmcts.sptribs.caseworker.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.caseworker.model.PostponeReason;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.common.config.WebMvcConfig;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.exception.NotificationException;
import uk.gov.hmcts.sptribs.testutil.IdamWireMock;
import uk.gov.service.notify.NotificationClientException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.emptySet;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType.EMAIL;
import static uk.gov.hmcts.sptribs.ciccase.model.HearingFormat.FACE_TO_FACE;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.REPRESENTATIVE;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.RESPONDENT;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.SUBJECT;
import static uk.gov.hmcts.sptribs.testutil.IdamWireMock.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.testutil.IdamWireMock.stubForIdamDetails;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASEWORKER_USER_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID_HYPHENATED;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getHearingList;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getMockedHearingVenueData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getMockedRegionData;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_POSTPONE_HEARING;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {IdamWireMock.PropertiesInitializer.class})
public class CaseworkerPostponeHearingIT {

    private static final String CASEWORKER_POSTPONE_HEARING_ABOUT_TO_START_RESPONSE =
        "classpath:responses/caseworker-postpone-hearing-about-to-start-response.json";
    private static final String CASEWORKER_POSTPONE_HEARING_ABOUT_TO_SUBMIT_RESPONSE =
        "classpath:responses/caseworker-postpone-hearing-about-to-submit-response.json";

    private static final String CONFIRMATION_HEADER = "$.confirmation_header";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WebMvcConfig webMvcConfig;

    @MockitoBean
    private NotificationServiceCIC notificationServiceCIC;

    @BeforeAll
    static void setUp() {
        IdamWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        IdamWireMock.stopAndReset();
    }

    @Test
    void shouldPopulateHearingListOnAboutToStart() throws Exception {
        final CaseData caseData = caseData();
        caseData.setHearingList(getHearingList());

        String response = mockMvc.perform(post(ABOUT_TO_START_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_POSTPONE_HEARING)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(CASEWORKER_POSTPONE_HEARING_ABOUT_TO_START_RESPONSE)));
    }

    @ParameterizedTest
    @EnumSource(PostponeReason.class)
    void shouldSuccessfullyPostponeHearingForAnyValidPostponeReason(PostponeReason postponeReason) throws Exception {
        final CaseData caseData = caseData();

        final Set<NotificationParties> parties = new HashSet<>();
        final CicCase cicCase = CicCase.builder()
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .hearingNotificationParties(parties)
            .hearingList(
                DynamicList.builder()
                    .value(
                        DynamicListElement.builder()
                            .label("1 - Final - 21 Apr 2021 10:00")
                            .build()
                    ).build()
            )
            .build();
        caseData.setCicCase(cicCase);

        final Listing listing = Listing.builder()
            .regionList(getMockedRegionData())
            .hearingVenues(getMockedHearingVenueData())
            .venueNotListedOption(emptySet())
            .roomAtVenue("G.01")
            .addlInstr("Ground floor")
            .hearingFormat(FACE_TO_FACE)
            .shortNotice(YES)
            .build();

        caseData.setListing(listing);
        caseData.setCicCase(cicCase);

        caseData.getListing().setPostponeReason(postponeReason);

        JSONObject responseAboutToSubmit = new JSONObject(expectedResponse(CASEWORKER_POSTPONE_HEARING_ABOUT_TO_SUBMIT_RESPONSE));
        JSONObject responseAboutToSubmitData = new JSONObject((responseAboutToSubmit.get("data")).toString());
        responseAboutToSubmitData.put("postponeReason", postponeReason.getReason());
        responseAboutToSubmit.put("data", responseAboutToSubmitData);

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, ST_CIC_CASEWORKER);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_POSTPONE_HEARING)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andExpect(
                content().json(responseAboutToSubmit.toString())
            );
    }

    @Test
    void shouldSuccessfullyDispatchNotificationsOnSubmitted() throws Exception {
        final CaseData caseData = caseData();
        caseData.setHyphenatedCaseRef(TEST_CASE_ID_HYPHENATED);
        caseData.setCicCase(
            CicCase.builder()
                .contactPreferenceType(EMAIL)
                .representativeContactDetailsPreference(EMAIL)
                .applicantContactDetailsPreference(EMAIL)
                .fullName("Test Name")
                .email("test@test.com")
                .representativeFullName("Rep Name")
                .representativeEmailAddress("representative@test.com")
                .respondentName("Respondent Name")
                .respondentEmail("respondent@test.com")
                .notifyPartySubject(Collections.singleton(SubjectCIC.SUBJECT))
                .notifyPartyRepresentative(Collections.singleton(RepresentativeCIC.REPRESENTATIVE))
                .notifyPartyRespondent(Collections.singleton(RespondentCIC.RESPONDENT))
                .hearingNotificationParties(
                    Set.of(SUBJECT, REPRESENTATIVE, RESPONDENT))
                .build()
        );

        String response = mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_POSTPONE_HEARING)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("""
                # Hearing Postponed\s
                ## The hearing has been postponed, the case has been updated\s
                ## A notification has been sent to: Representative, Respondent, Subject""");

        verify(notificationServiceCIC, times(3)).sendEmail(any(), eq(TEST_CASE_ID_HYPHENATED), eq(null));
        verifyNoMoreInteractions(notificationServiceCIC);
    }

    @Test
    void shouldReturnErrorMessageIfNotificationsFailOnSubmitted() throws Exception {
        final CaseData caseData = CaseData.builder()
            .hyphenatedCaseRef(TEST_CASE_ID_HYPHENATED)
            .cicCase(CicCase.builder()
                .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
                .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
                .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
                .build())
            .build();

        when(notificationServiceCIC.sendEmail(any(), eq(TEST_CASE_ID_HYPHENATED), eq(null)))
            .thenThrow(new NotificationException(new NotificationClientException("")));

        String response = mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_POSTPONE_HEARING)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Postpone hearing notification failed")
            .contains("## A notification could not be sent to: Representative, Respondent, Subject")
            .contains("## Please resend the notification.");
    }
}
