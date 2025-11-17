package uk.gov.hmcts.sptribs.caseworker.event.page;

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
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.config.WebMvcConfig;
import uk.gov.hmcts.sptribs.testutil.IdamWireMock;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_ISSUE_FINAL_DECISION;
import static uk.gov.hmcts.sptribs.ciccase.model.DecisionTemplate.GENERAL_DIRECTIONS;
import static uk.gov.hmcts.sptribs.ciccase.model.DecisionTemplate.PRO_FORMA_SUMMONS;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.PRO_FORMA_MAIN_CONTENT;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ISSUE_FINAL_DECISION_SELECT_TEMPLATE_MID_EVENT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.callbackRequest;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {IdamWireMock.PropertiesInitializer.class})
public class IssueFinalDecisionSelectTemplateIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WebMvcConfig webMvcConfig;

    @BeforeAll
    static void setUp() {
        IdamWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        IdamWireMock.stopAndReset();
    }

    @Test
    void shouldSetDecisionMainContentToEmptyStringIfDecisionGeneralDirectionsOnMidEvent() throws Exception {
        final CaseData caseData = CaseData.builder()
            .caseIssueFinalDecision(CaseIssueFinalDecision.builder()
                .decisionTemplate(GENERAL_DIRECTIONS)
                .build()
            )
            .build();

        mockMvc.perform(post(ISSUE_FINAL_DECISION_SELECT_TEMPLATE_MID_EVENT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CASEWORKER_ISSUE_FINAL_DECISION)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andExpect(
                jsonPath("$.data.decisionMainContent")
                    .value("")
            );
    }

    @Test
    void shouldPopulateDecisionMainContentOnMidEvent() throws Exception {
        final CaseData caseData = CaseData.builder()
            .caseIssueFinalDecision(CaseIssueFinalDecision.builder()
                .decisionTemplate(PRO_FORMA_SUMMONS)
                .build()
            )
            .build();

        mockMvc.perform(post(ISSUE_FINAL_DECISION_SELECT_TEMPLATE_MID_EVENT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CASEWORKER_ISSUE_FINAL_DECISION)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andExpect(
                jsonPath("$.data.decisionMainContent")
                    .value(PRO_FORMA_MAIN_CONTENT)
            );
    }
}
