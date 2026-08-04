package uk.gov.hmcts.sptribs.caseworker.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.config.WebMvcConfig;
import uk.gov.hmcts.sptribs.idam.CICUser;
import uk.gov.hmcts.sptribs.idam.IdamService;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.sptribs.testutil.ITEventConstants.CASEWORKER_ADD_NOTE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.feignException;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class CaseworkerAddNoteIT {

    private static final String CASEWORKER_ADD_NOTE_RESPONSE =
        "classpath:responses/caseworker-add-note-response.json";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WebMvcConfig webMvcConfig;

    @MockitoBean
    private Clock clock;

    @MockitoBean
    private IdamService idamService;

    @BeforeEach
    void setClock() {
        when(clock.instant()).thenReturn(Instant.parse("2021-06-18T12:00:00.000Z"));
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
    }

    @Test
    void shouldSuccessfullyAddCaseNoteToCaseDataWhenThereAreNoExistingCaseNotes() throws Exception {
        final CaseData caseData = caseData();
        caseData.setNote("This is a test note");

        final CICUser user = new CICUser(
            TEST_AUTHORIZATION_TOKEN,
            UserInfo.builder()
                .name("forename Surname")
                .givenName("forename")
                .familyName("Surname")
                .build()
        );

        when(idamService.retrieveUser(eq(TEST_AUTHORIZATION_TOKEN))).thenReturn(user);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CASEWORKER_ADD_NOTE)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andExpect(
                content().json(expectedResponse(CASEWORKER_ADD_NOTE_RESPONSE))
            );
    }

    @Test
    void shouldThrowUnauthorizedExceptionWhenAboutToSubmitIsInvokedAndIdamThrowsUnauthorized() throws Exception {
        final CaseData caseData = caseData();
        caseData.setNote("This is a test note");

        when(idamService.retrieveUser(eq(TEST_AUTHORIZATION_TOKEN)))
            .thenThrow(feignException(401, "Invalid idam credentials"));

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CASEWORKER_ADD_NOTE)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isUnauthorized()
            )
            .andExpect(
                result -> assertThat(result.getResolvedException()).isExactlyInstanceOf(FeignException.Unauthorized.class)
            )
            .andExpect(
                result -> assertThat(requireNonNull(result.getResolvedException()).getMessage())
                    .contains("Invalid idam credentials")
            );
    }
}
