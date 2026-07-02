package uk.gov.hmcts.sptribs.caseworker.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ccd.sdk.type.FlagDetail;
import uk.gov.hmcts.ccd.sdk.type.Flags;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.config.WebMvcConfig;
import uk.gov.hmcts.sptribs.common.service.CcdSupplementaryDataService;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.hmcts.sptribs.testutil.IdamWireMock;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.sptribs.caseworker.util.CaseFlagsUtil.ANONYMITY_FLAG_CODE;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CASE_FLAG;
import static uk.gov.hmcts.sptribs.notification.TemplateName.ANONYMITY_APPLIED_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID_HYPHENATED;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {IdamWireMock.PropertiesInitializer.class})
public class CaseworkerCaseFlagIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WebMvcConfig webMvcConfig;

    @MockitoBean
    private CcdSupplementaryDataService coreCaseApiService;

    @MockitoBean
    private NotificationServiceCIC notificationServiceCIC;

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
    void shouldReturnErrorMessageIfNotificationsFailOnSubmitted() throws Exception {
        String response = mockMvc.perform(post(SUBMITTED_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData(),
                    CASEWORKER_CASE_FLAG)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Flag created \n## This Flag has been added to case");

        verify(coreCaseApiService).submitSupplementaryDataToCcd(TEST_CASE_ID.toString());
    }

    @Test
    void shouldMergeAnonymityFlagsPreservingOriginalIdOnAboutToSubmit() throws Exception {
        CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder()
                .anonymiseYesOrNo(YesOrNo.YES)
                .anonymisedAppellantName("AC")
                .anonymisationDate(LocalDate.of(2024, 1, 1))
                .build())
            .caseFlags(Flags.builder().details(List.of(
                buildAnonymityFlag("new-flag-id", "Active", "Latest comment", LocalDateTime.of(2025, 1, 1, 10, 0)),
                buildAnonymityFlag("original-flag-id", "Inactive", "Original comment", LocalDateTime.of(2024, 1, 1, 10, 0))
            )).build())
            .build();

        CaseData beforeData = CaseData.builder()
            .cicCase(CicCase.builder()
                .anonymiseYesOrNo(YesOrNo.YES)
                .anonymisedAppellantName("AC")
                .anonymisationDate(LocalDate.of(2024, 1, 1))
                .build())
            .caseFlags(Flags.builder().details(List.of(
                buildAnonymityFlag("original-flag-id", "Inactive", "Original comment", LocalDateTime.of(2024, 1, 1, 10, 0))
            )).build())
            .build();

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(caseData, beforeData, CASEWORKER_CASE_FLAG)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response).inPath("$.data.caseFlags.details").isArray().hasSize(1);
        assertThatJson(response).inPath("$.data.caseFlags.details[0].id").isEqualTo("original-flag-id");
        assertThatJson(response).inPath("$.data.caseFlags.details[0].value.status").isEqualTo("Active");
        assertThatJson(response).inPath("$.data.caseFlags.details[0].value.flagComment").isEqualTo("Latest comment");

        LocalDateTime dateCreated = OffsetDateTime.parse(
            objectMapper.readTree(response).at("/data/caseFlags/details/0/value/dateTimeCreated").asText()
        ).toLocalDateTime();
        LocalDateTime dateModified = OffsetDateTime.parse(
            objectMapper.readTree(response).at("/data/caseFlags/details/0/value/dateTimeModified").asText()
        ).toLocalDateTime();
        assertThat(dateCreated).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
        assertThat(dateModified).isAfter(dateCreated);
    }

    private ListValue<FlagDetail> buildAnonymityFlag(String id, String status, String comment, LocalDateTime createdAt) {
        return ListValue.<FlagDetail>builder()
            .id(id)
            .value(FlagDetail.builder()
                .flagCode(ANONYMITY_FLAG_CODE)
                .status(status)
                .flagComment(comment)
                .hearingRelevant(YesOrNo.YES)
                .dateTimeCreated(createdAt)
                .build())
            .build();
    }

    @Test
    void shouldDispatchAnonymityEmailWhenAnonymityNewlyAppliedOnSubmitted() throws Exception {
        final CaseData caseData = CaseData.builder()
            .caseNumber(TEST_CASE_ID.toString())
            .hyphenatedCaseRef(TEST_CASE_ID_HYPHENATED)
            .cicCase(CicCase.builder()
                .anonymiseYesOrNo(YesOrNo.YES)
                .anonymisedAppellantName("AAC")
                .fullName("Test Name")
                .build())
            .build();

        final CaseData caseDataBefore = CaseData.builder()
            .caseNumber(TEST_CASE_ID.toString())
            .hyphenatedCaseRef(TEST_CASE_ID_HYPHENATED)
            .cicCase(CicCase.builder()
                .anonymiseYesOrNo(YesOrNo.NO)
                .fullName("Test Name")
                .build())
            .build();

        String response = mockMvc.perform(post(SUBMITTED_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    caseDataBefore,
                    CASEWORKER_CASE_FLAG)))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Flag created \n## This Flag has been added to case");

        verify(notificationServiceCIC, times(1)).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID_HYPHENATED));
    }

    @Test
    void shouldNotDispatchAnonymityEmailWhenAnonymityAlreadyAppliedOnSubmitted() throws Exception {
        final CaseData caseData = CaseData.builder()
            .caseNumber(TEST_CASE_ID.toString())
            .hyphenatedCaseRef(TEST_CASE_ID_HYPHENATED)
            .cicCase(CicCase.builder()
                .anonymiseYesOrNo(YesOrNo.YES)
                .anonymisedAppellantName("AAC")
                .anonymityAlreadyApplied(YesOrNo.YES)
                .fullName("Test Name")
                .build())
            .build();

        final CaseData caseDataBefore = CaseData.builder()
            .caseNumber(TEST_CASE_ID.toString())
            .hyphenatedCaseRef(TEST_CASE_ID_HYPHENATED)
            .cicCase(CicCase.builder()
                .anonymiseYesOrNo(YesOrNo.YES)
                .anonymityAlreadyApplied(YesOrNo.YES)
                .anonymisedAppellantName("AAC")
                .fullName("Test Name")
                .build())
            .build();

        String response = mockMvc.perform(post(SUBMITTED_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    caseDataBefore,
                    CASEWORKER_CASE_FLAG)))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Flag created \n## This Flag has been added to case");

        verifyNoInteractions(notificationServiceCIC);
    }
}
