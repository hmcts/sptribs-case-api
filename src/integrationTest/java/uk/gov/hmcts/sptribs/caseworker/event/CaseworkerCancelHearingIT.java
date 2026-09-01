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
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.config.WebMvcConfig;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.dispatcher.NotificationDispatcher;
import uk.gov.hmcts.sptribs.testutil.IdamWireMock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CANCEL_HEARING;
import static uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType.EMAIL;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.APPLICANT;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.REPRESENTATIVE;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.RESPONDENT;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.SUBJECT;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID_HYPHENATED;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getHearingList;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getRecordListing;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {IdamWireMock.PropertiesInitializer.class})
public class CaseworkerCancelHearingIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WebMvcConfig webMvcConfig;

    @MockitoBean
    private NotificationServiceCIC notificationServiceCIC;

    private static final String CASEWORKER_CANCEL_HEARING_ABOUT_TO_START_RESPONSE =
        "classpath:responses/caseworker-cancel-hearing-about-to-start-response.json";
    private static final String CASEWORKER_CANCEL_HEARING_ABOUT_TO_SUBMIT_RESPONSE =
        "classpath:responses/caseworker-cancel-hearing-about-to-submit-response.json";

    private static final String CONFIRMATION_HEADER = "$.confirmation_header";

    @BeforeAll
    static void setUp() {
        IdamWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        IdamWireMock.stopAndReset();
    }

    @Test
    void shouldPopulateCicCaseHearingListOnAboutToStart() throws Exception {
        final CaseData caseData = caseData();
        caseData.setHearingList(getHearingList());

        String response = mockMvc.perform(post(ABOUT_TO_START_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CASEWORKER_CANCEL_HEARING)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(CASEWORKER_CANCEL_HEARING_ABOUT_TO_START_RESPONSE)));
    }

    @Test
    void shouldClearPreviouslyPopulatedHearingOptionsOnAboutToSubmit() throws Exception {
        final CaseData caseData = caseData();
        caseData.setHearingList(getHearingList());
        DynamicListElement hearingElement1 = DynamicListElement.builder()
            .code(UUID.randomUUID())
            .label("1 - Final - 14 Aug 2024 10:00")
            .build();
        DynamicListElement hearingElement2 = DynamicListElement.builder()
            .code(UUID.randomUUID())
            .label("2 - Interlocutory - 14 Aug 2024 14:00")
            .build();
        DynamicList hearingDynamicList = DynamicList.builder()
            .value(hearingElement2)
            .listItems(List.of(hearingElement1, hearingElement2))
            .build();
        caseData.getCicCase().setHearingList(hearingDynamicList);


        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CASEWORKER_CANCEL_HEARING)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(CASEWORKER_CANCEL_HEARING_ABOUT_TO_SUBMIT_RESPONSE)));
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
                .applicantFullName("Applicant Name")
                .applicantEmailAddress("applicant@test.com")
                .hearingNotificationParties(
                    Set.of(SUBJECT, REPRESENTATIVE, APPLICANT, RESPONDENT))
                .build()
        );

        String response = mockMvc.perform(post(SUBMITTED_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CASEWORKER_CANCEL_HEARING)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Hearing cancelled \n"
                + "## A notification has been sent to:")
            .contains("Subject")
            .contains("Respondent")
            .contains("Representative")
            .contains("Applicant");

        verify(notificationServiceCIC, times(4)).sendEmail(any(), eq(TEST_CASE_ID_HYPHENATED), eq(null));
        verifyNoMoreInteractions(notificationServiceCIC);
    }

    @Test
    void shouldReturnErrorMessageIfNotificationsFailOnSubmitted() throws Exception {
        final CaseData caseData = caseData();
        caseData.setHyphenatedCaseRef(TEST_CASE_ID_HYPHENATED);
        caseData.setCicCase(
            CicCase.builder()
                .representativeFullName("Rep Name")
                .respondentName("Respondent Name")
                .applicantFullName("Applicant Name")
                .hearingNotificationParties(
                    Set.of(SUBJECT, REPRESENTATIVE, APPLICANT, RESPONDENT))
                .build()
        );

        String response = mockMvc.perform(post(SUBMITTED_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CASEWORKER_CANCEL_HEARING)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Cancel hearing notification failed")
            .contains("## Please resend the notification")
            .contains("Representative")
            .contains("Applicant")
            .contains("Subject");
    }

    @Test
    void shouldKeepEarliestHearingDateWhenOneOfMultipleIsCancelled() throws Exception {
        final Listing hearing1 = getRecordListing(); // Listed, 2023-04-21
        final Listing hearing2 = getRecordListing();
        hearing2.setDate(LocalDate.of(2023, 6, 15));

        final List<ListValue<Listing>> hearingList = new ArrayList<>();
        hearingList.add(new ListValue<>("1", hearing1));
        hearingList.add(new ListValue<>("2", hearing2));

        final CaseData caseData = caseData();
        caseData.setHearingList(hearingList);
        caseData.setHearingDate(LocalDate.of(2023, 4, 21));
        caseData.setListing(new Listing());
        caseData.getCicCase().setHearingList(
            DynamicList.builder()
                .value(DynamicListElement.builder()
                    .label("2 - Final - 15 Jun 2023 10:00")
                    .build())
                .listItems(List.of(
                    DynamicListElement.builder().label("1 - Final - 21 Apr 2023 10:00").build(),
                    DynamicListElement.builder().label("2 - Final - 15 Jun 2023 10:00").build()
                ))
                .build()
        );

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(caseData, CASEWORKER_CANCEL_HEARING)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .inPath("$.data.hearingDate")
            .isEqualTo("2023-04-21");
    }

    @Test
    void shouldMoveHearingDateToNextEarliestWhenEarliestIsCancelled() throws Exception {
        final Listing hearing1 = getRecordListing();
        final Listing hearing2 = getRecordListing();
        hearing2.setDate(LocalDate.of(2023, 6, 15));

        final List<ListValue<Listing>> hearingList = new ArrayList<>();
        hearingList.add(new ListValue<>("1", hearing1));
        hearingList.add(new ListValue<>("2", hearing2));

        final CaseData caseData = caseData();
        caseData.setHearingList(hearingList);
        caseData.setHearingDate(LocalDate.of(2023, 4, 21));
        caseData.setListing(new Listing());
        caseData.getCicCase().setHearingList(
            DynamicList.builder()
                .value(DynamicListElement.builder()
                    .label("1 - Final - 21 Apr 2023 10:00")
                    .build())
                .listItems(List.of(
                    DynamicListElement.builder().label("1 - Final - 21 Apr 2023 10:00").build(),
                    DynamicListElement.builder().label("2 - Final - 15 Jun 2023 10:00").build()
                ))
                .build()
        );

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(caseData, CASEWORKER_CANCEL_HEARING)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .inPath("$.data.hearingDate")
            .isEqualTo("2023-06-15");
    }
}
