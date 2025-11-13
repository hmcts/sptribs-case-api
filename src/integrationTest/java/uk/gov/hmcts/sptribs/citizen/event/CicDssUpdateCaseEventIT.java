package uk.gov.hmcts.sptribs.citizen.event;

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
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.common.config.WebMvcConfig;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.exception.NotificationException;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.hmcts.sptribs.testutil.IdamWireMock;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CITIZEN_DSS_UPDATE_CASE_SUBMISSION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getDssCaseData;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {IdamWireMock.PropertiesInitializer.class})
public class CicDssUpdateCaseEventIT {

    private static final String CITIZEN_DSS_UPDATE_CASE_ABOUT_TO_SUBMIT_RESPONSE =
        "classpath:responses/citizen-dss-update-case-about-to-submit-response.json";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WebMvcConfig webMvcConfig;

    @MockitoBean
    private IdamService idamService;

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
    void shouldAddDocumentsToCaseDataInAboutToSubmit() throws Exception {
        final CaseData caseData = caseData();
        final DssCaseData dssCaseData = DssCaseData.builder()
            .otherInfoDocuments(getDssCaseData().getOtherInfoDocuments())
            .additionalInformation("Additional information added in DDS Update Case")
            .build();
        caseData.setDssCaseData(dssCaseData);

        final User user = new User(
            TEST_AUTHORIZATION_TOKEN,
            UserDetails.builder()
                .forename("Test")
                .surname("User")
                .build()
        );

        when(idamService.retrieveUser(eq(TEST_AUTHORIZATION_TOKEN)))
            .thenReturn(user);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CITIZEN_DSS_UPDATE_CASE_SUBMISSION)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andDo(print())
            .andExpect(
                content().json(expectedResponse(CITIZEN_DSS_UPDATE_CASE_ABOUT_TO_SUBMIT_RESPONSE))
            );
    }

    @Test
    void shouldSuccessfullyDispatchNotificationsInSubmitted() throws Exception {
        final CaseData caseData = caseData();

        String response = mockMvc.perform(post(SUBMITTED_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CITIZEN_DSS_UPDATE_CASE_SUBMISSION)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();


        assertThatJson(response)
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# CIC Dss Update Case Event Email notifications sent");

        verify(notificationServiceCIC, times(2)).sendEmail(any());
        verifyNoMoreInteractions(notificationServiceCIC);
    }

    @Test
    void shouldReturnErrorMessageIfNotificationsFailInSubmitted() throws Exception {
        final CaseData caseData = caseData();

        doThrow(NotificationException.class)
            .when(notificationServiceCIC)
            .sendEmail(any(NotificationRequest.class));

        String response = mockMvc.perform(post(SUBMITTED_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CITIZEN_DSS_UPDATE_CASE_SUBMISSION)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# CIC Dss Update Case Event Email notification failed %n## Please resend the notification");
    }
}
