package uk.gov.hmcts.sptribs.citizen.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.sptribs.caseworker.model.EditCicaCaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.common.config.EmailTemplatesConfigCIC;
import uk.gov.hmcts.sptribs.common.config.WebMvcConfig;
import uk.gov.hmcts.sptribs.common.service.CcdSupplementaryDataService;
import uk.gov.hmcts.sptribs.testutil.IdamWireMock;
import uk.gov.service.notify.NotificationClient;

import java.lang.constant.Constable;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CITIZEN_CIC_SUBMIT_CASE;
import static uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CICA_REF_NUMBER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_NUMBER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_REPRESENTATIVE_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_SUBJECT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CONTACT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CONTACT_PARTY_INFO;
import static uk.gov.hmcts.sptribs.common.CommonConstants.DASHBOARD_KEY;
import static uk.gov.hmcts.sptribs.common.CommonConstants.HAS_CICA_NUMBER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_NAME;
import static uk.gov.hmcts.sptribs.common.ccd.CcdCaseType.CIC;
import static uk.gov.hmcts.sptribs.testutil.IdamWireMock.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.testutil.IdamWireMock.stubForIdamDetails;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASEWORKER_USER_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID_HYPHENATED;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CICA_REF_NUMBER;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {IdamWireMock.PropertiesInitializer.class})
@ActiveProfiles("integration")
public class CicSubmitCaseEventIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Environment environment;

    @MockitoBean
    private WebMvcConfig webMvcConfig;

    @MockitoBean
    private NotificationClient notificationClient;

    @MockitoBean
    private EmailTemplatesConfigCIC emailTemplatesConfig;

    @MockitoBean
    private CcdSupplementaryDataService ccdSupplementaryDataService;

    @Captor
    private ArgumentCaptor<Map<String, Object>> templateVarsCaptor;

    @BeforeAll
    static void setUp() {
        IdamWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        IdamWireMock.stopAndReset();
    }

    @BeforeEach
    void setTestData() {
        Map<String, String> templatesCIC = new HashMap<>();
        templatesCIC.put("APPLICATION_RECEIVED", "48ccf890-0550-48ca-8c52-fa68cec09947");
        templatesCIC.put("APPLICATION_RECEIVED_CY", "86e6988c-dfc8-43de-8890-e38269ee40d1");
        templatesCIC.put("APPLICATION_RECEIVED_NEW_CD","9b331dd4-9ec6-4aba-9f48-9a0ef760f80f");

        when(emailTemplatesConfig.getTemplatesCIC()).thenReturn(templatesCIC);
    }

    @Test
    void dashboardFeaturePropertyExplicitlyConfigured() {
        assertThat(
            environment.getProperty("feature.citizen-dashboard.enabled"))
            .as("feature.citizen-dashboard.enabled must be set in application-integration.yaml")
            .isNotNull();
    }

    @Nested
    class WhenCitizenDashboardDisabled {

        @Autowired
        Environment environment;

        @BeforeEach
        void onlyRunWhenDisabled() {
            assumeFalse(environment.getProperty("feature.citizen-dashboard.enabled", Boolean.class, false),
                "Skipping: feature.citizen-dashboard.enabled is currently true");
        }

        @Test
        void shouldSendSubjectEmailInEnglishWhenLanguagePreferenceIsEnglish() throws Exception {
            final CaseData caseData = caseData();
            final DssCaseData dssCaseData = DssCaseData.builder()
                .subjectFullName("Test Subject")
                .subjectEmailAddress("test@subject.com")
                .notifyPartyMessage("A message")
                .languagePreference(ENGLISH)
                .build();
            final EditCicaCaseDetails cicaCaseDetails = EditCicaCaseDetails.builder().cicaReferenceNumber(TEST_CICA_REF_NUMBER).build();
            caseData.setDssCaseData(dssCaseData);
            caseData.setHyphenatedCaseRef(TEST_CASE_ID_HYPHENATED);
            caseData.setEditCicaCaseDetails(cicaCaseDetails);

            stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, ST_CIC_CASEWORKER);

            mockMvc.perform(post(SUBMITTED_URL)
                    .contentType(APPLICATION_JSON)
                    .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                    .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                    .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            CITIZEN_CIC_SUBMIT_CASE)))
                    .accept(APPLICATION_JSON))
                .andExpect(
                    status().isOk());

            Map<String, ? extends Constable> templateVars = Map.of(
                TRIBUNAL_NAME, CIC,
                CONTACT_PARTY_INFO, "A message",
                CIC_CASE_SUBJECT_NAME, "Test Subject",
                CONTACT_NAME, "Test Subject",
                CIC_CASE_NUMBER, TEST_CASE_ID_HYPHENATED,
                HAS_CICA_NUMBER, true,
                CICA_REF_NUMBER, TEST_CICA_REF_NUMBER
            );
            verify(notificationClient).sendEmail(
                eq("48ccf890-0550-48ca-8c52-fa68cec09947"),
                eq("test@subject.com"),
                templateVarsCaptor.capture(),
                anyString()
            );
            assertThat(templateVarsCaptor.getValue())
                .doesNotContainKey(DASHBOARD_KEY)
                .containsAllEntriesOf(templateVars);
        }

        @Test
        void shouldSendSubjectEmailInWelshWhenLanguagePreferenceIsWelsh() throws Exception {
            final CaseData caseData = caseData();
            final DssCaseData dssCaseData = DssCaseData.builder()
                .subjectFullName("Test Subject")
                .subjectEmailAddress("test@subject.com")
                .notifyPartyMessage("A message")
                .languagePreference(WELSH)
                .build();
            final EditCicaCaseDetails cicaCaseDetails = EditCicaCaseDetails.builder().cicaReferenceNumber(TEST_CICA_REF_NUMBER).build();
            caseData.setDssCaseData(dssCaseData);
            caseData.setHyphenatedCaseRef(TEST_CASE_ID_HYPHENATED);
            caseData.setEditCicaCaseDetails(cicaCaseDetails);

            stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, ST_CIC_CASEWORKER);

            mockMvc.perform(post(SUBMITTED_URL)
                    .contentType(APPLICATION_JSON)
                    .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                    .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                    .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            CITIZEN_CIC_SUBMIT_CASE)))
                    .accept(APPLICATION_JSON))
                .andExpect(
                    status().isOk());

            Map<String, ? extends Constable> templateVars = Map.of(
                TRIBUNAL_NAME, CIC,
                CONTACT_PARTY_INFO, "A message",
                CIC_CASE_SUBJECT_NAME, "Test Subject",
                CONTACT_NAME, "Test Subject",
                CIC_CASE_NUMBER, TEST_CASE_ID_HYPHENATED,
                HAS_CICA_NUMBER, true,
                CICA_REF_NUMBER, TEST_CICA_REF_NUMBER
            );
            verify(notificationClient).sendEmail(
                eq("86e6988c-dfc8-43de-8890-e38269ee40d1"),
                eq("test@subject.com"),
                templateVarsCaptor.capture(),
                anyString()
            );
            assertThat(templateVarsCaptor.getValue())
                .doesNotContainKey(DASHBOARD_KEY)
                .containsAllEntriesOf(templateVars);
        }

        @Test
        void shouldSendRepresentativeEmail() throws Exception {
            final CaseData caseData = caseData();
            final DssCaseData dssCaseData = DssCaseData.builder()
                .subjectFullName("Test Subject")
                .representativeFullName("Test Representative")
                .representativeEmailAddress("test@representative.com")
                .notifyPartyMessage("A message")
                .build();
            final EditCicaCaseDetails cicaCaseDetails = EditCicaCaseDetails.builder().cicaReferenceNumber(TEST_CICA_REF_NUMBER).build();
            caseData.setDssCaseData(dssCaseData);
            caseData.setHyphenatedCaseRef(TEST_CASE_ID_HYPHENATED);
            caseData.setEditCicaCaseDetails(cicaCaseDetails);

            stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, ST_CIC_CASEWORKER);

            mockMvc.perform(post(SUBMITTED_URL)
                    .contentType(APPLICATION_JSON)
                    .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                    .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                    .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            CITIZEN_CIC_SUBMIT_CASE)))
                    .accept(APPLICATION_JSON))
                .andExpect(
                    status().isOk());

            Map<String, ? extends Constable> templateVars = Map.of(
                TRIBUNAL_NAME, CIC,
                CONTACT_PARTY_INFO, "A message",
                CIC_CASE_SUBJECT_NAME, "Test Subject",
                CIC_CASE_REPRESENTATIVE_NAME, "Test Representative",
                CONTACT_NAME, "Test Representative",
                CIC_CASE_NUMBER, TEST_CASE_ID_HYPHENATED,
                HAS_CICA_NUMBER, true,
                CICA_REF_NUMBER, TEST_CICA_REF_NUMBER
            );
            verify(notificationClient).sendEmail(
                eq("48ccf890-0550-48ca-8c52-fa68cec09947"),
                eq("test@representative.com"),
                templateVarsCaptor.capture(),
                anyString()
            );
            assertThat(templateVarsCaptor.getValue())
                .doesNotContainKey(DASHBOARD_KEY)
                .containsAllEntriesOf(templateVars);
        }
    }

    @Nested
    class WhenCitizenDashboardEnabled {

        @Autowired
        Environment environment;

        @Value("${sptribs-frontend.dashboard-url}")
        private String citizenDashboardUrl;

        @BeforeEach
        void onlyRunWhenEnabled() {
            assumeTrue(environment.getProperty("feature.citizen-dashboard.enabled", Boolean.class, false),
                "Skipping: feature.citizen-dashboard.enabled is currently false");
        }

        @Test
        void shouldSendSubjectEmailInEnglishWhenLanguagePreferenceIsEnglish() throws Exception {
            final CaseData caseData = caseData();
            final DssCaseData dssCaseData = DssCaseData.builder()
                .subjectFullName("Test Subject")
                .subjectEmailAddress("test@subject.com")
                .notifyPartyMessage("A message")
                .languagePreference(ENGLISH)
                .build();
            final EditCicaCaseDetails cicaCaseDetails = EditCicaCaseDetails.builder().cicaReferenceNumber(TEST_CICA_REF_NUMBER).build();
            caseData.setDssCaseData(dssCaseData);
            caseData.setHyphenatedCaseRef(TEST_CASE_ID_HYPHENATED);
            caseData.setEditCicaCaseDetails(cicaCaseDetails);

            stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, ST_CIC_CASEWORKER);

            mockMvc.perform(post(SUBMITTED_URL)
                    .contentType(APPLICATION_JSON)
                    .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                    .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                    .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            CITIZEN_CIC_SUBMIT_CASE)))
                    .accept(APPLICATION_JSON))
                .andExpect(
                    status().isOk());

            verify(notificationClient).sendEmail(
                eq("9b331dd4-9ec6-4aba-9f48-9a0ef760f80f"),
                eq("test@subject.com"),
                templateVarsCaptor.capture(),
                anyString()
            );
            assertThat(templateVarsCaptor.getValue())
                .containsEntry(DASHBOARD_KEY, citizenDashboardUrl)
                .containsEntry(HAS_CICA_NUMBER, true)
                .containsEntry(CIC_CASE_NUMBER, TEST_CASE_ID_HYPHENATED);
        }

        @Test
        void shouldSendRepresentativeEmail() throws Exception {
            final CaseData caseData = caseData();
            final DssCaseData dssCaseData = DssCaseData.builder()
                .subjectFullName("Test Subject")
                .representativeFullName("Test Representative")
                .representativeEmailAddress("test@representative.com")
                .notifyPartyMessage("A message")
                .build();
            final EditCicaCaseDetails cicaCaseDetails = EditCicaCaseDetails.builder().cicaReferenceNumber(TEST_CICA_REF_NUMBER).build();
            caseData.setDssCaseData(dssCaseData);
            caseData.setHyphenatedCaseRef(TEST_CASE_ID_HYPHENATED);
            caseData.setEditCicaCaseDetails(cicaCaseDetails);

            stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, ST_CIC_CASEWORKER);

            mockMvc.perform(post(SUBMITTED_URL)
                    .contentType(APPLICATION_JSON)
                    .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                    .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                    .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            CITIZEN_CIC_SUBMIT_CASE)))
                    .accept(APPLICATION_JSON))
                .andExpect(
                    status().isOk());

            verify(notificationClient).sendEmail(
                eq("9b331dd4-9ec6-4aba-9f48-9a0ef760f80f"),
                eq("test@representative.com"),
                templateVarsCaptor.capture(),
                anyString()
            );

            assertThat(templateVarsCaptor.getValue())
                .containsEntry(DASHBOARD_KEY, citizenDashboardUrl)
                .containsEntry(HAS_CICA_NUMBER, true)
                .containsEntry(CIC_CASE_NUMBER, TEST_CASE_ID_HYPHENATED);
        }
    }
}
