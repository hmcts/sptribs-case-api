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
import uk.gov.hmcts.ccd.sdk.type.FlagDetail;
import uk.gov.hmcts.ccd.sdk.type.Flags;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.config.WebMvcConfig;
import uk.gov.hmcts.sptribs.testutil.IdamWireMock;

import java.time.LocalDateTime;
import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.sptribs.caseworker.util.CaseFlagsUtil.ANONYMITY_FLAG_CODE;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_MANAGE_CASE_FLAG;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {IdamWireMock.PropertiesInitializer.class})
public class CaseworkerManageCaseFlagIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WebMvcConfig webMvcConfig;

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
    void shouldReturnSuccessMessageOnSubmitted() throws Exception {
        String response = mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData(),
                        CASEWORKER_MANAGE_CASE_FLAG)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Flag updated");
    }

    @Test
    void shouldSetAnonymiseNoAndMergeInactiveAnonymityFlagsOnAboutToSubmit() throws Exception {
        CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder().anonymiseYesOrNo(YesOrNo.YES).anonymityAlreadyApplied(YesOrNo.YES).build())
            .caseFlags(Flags.builder().details(List.of(
                buildAnonymityFlag("new-id", "Inactive", LocalDateTime.of(2025, 1, 1, 10, 0)),
                buildAnonymityFlag("original-id", "Inactive", LocalDateTime.of(2024, 1, 1, 10, 0))
            )).build())
            .build();

        CaseData beforeData = CaseData.builder()
            .caseFlags(Flags.builder().details(List.of(
                buildAnonymityFlag("original-id", "Inactive", LocalDateTime.of(2024, 1, 1, 10, 0))
            )).build())
            .build();

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(caseData, beforeData, CASEWORKER_MANAGE_CASE_FLAG)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response).inPath("$.data.caseFlags.details").isArray().hasSize(1);
        assertThatJson(response).inPath("$.data.caseFlags.details[0].id").isEqualTo("original-id");
        assertThatJson(response).inPath("$.data.caseFlags.details[0].value.status").isEqualTo("Inactive");
        assertThatJson(response).inPath("$.data.cicCaseAnonymiseYesOrNo").isEqualTo("No");
    }

    private ListValue<FlagDetail> buildAnonymityFlag(String id, String status, LocalDateTime createdAt) {
        return ListValue.<FlagDetail>builder()
            .id(id)
            .value(FlagDetail.builder()
                .flagCode(ANONYMITY_FLAG_CODE)
                .status(status)
                .dateTimeCreated(createdAt)
                .build())
            .build();
    }
}
