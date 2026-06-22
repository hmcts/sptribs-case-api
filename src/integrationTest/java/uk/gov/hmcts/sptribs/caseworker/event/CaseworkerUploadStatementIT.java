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
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.config.WebMvcConfig;
import uk.gov.hmcts.sptribs.statement.model.StatementUpload;
import uk.gov.hmcts.sptribs.testutil.IdamWireMock;

import java.util.List;
import java.util.UUID;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.sptribs.testutil.ITEventConstants.CASEWORKER_UPLOAD_STATEMENT;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {IdamWireMock.PropertiesInitializer.class})
class CaseworkerUploadStatementIT {

    private static final String MID_EVENT_URL = "/callbacks/mid-event?page=statementUploadDocument";
    private static final String ABOUT_TO_START_RESPONSE =
        "classpath:responses/caseworker-upload-statement-about-to-start-response.json";
    private static final String ABOUT_TO_SUBMIT_VALIDATION_ERROR_RESPONSE =
        "classpath:responses/caseworker-upload-statement-about-to-submit-validation-error-response.json";
    private static final String MID_EVENT_VALID_RESPONSE =
        "classpath:responses/caseworker-upload-statement-mid-event-valid-response.json";
    private static final String MID_EVENT_INVALID_RESPONSE =
        "classpath:responses/caseworker-upload-statement-mid-event-invalid-response.json";
    private static final String CONFIRMATION_HEADER = "$.confirmation_header";

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
    void shouldPopulateStatementPartyListOnAboutToStart() throws Exception {
        CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder()
                .applicantFullName("Applicant Person")
                .respondentName("Appeals Team")
                .representativeFullName("Representative Person")
                .build())
            .build();

        String response = mockMvc.perform(post(ABOUT_TO_START_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, CASEWORKER_UPLOAD_STATEMENT)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(ABOUT_TO_START_RESPONSE)));
    }

    @Test
    void shouldReturnValidationErrorOnAboutToSubmitWhenDocumentIsInvalid() throws Exception {
        CaseData caseData = caseData();
        caseData.setCicCase(CicCase.builder()
            .applicantFullName("Applicant Person")
            .respondentName("Appeals Team")
            .representativeFullName("Representative Person")
            .build());
        caseData.setStatementUpload(StatementUpload.builder()
            .statementParty(DynamicList.builder()
                .value(DynamicListElement.builder().label("Applicant").code(UUID.randomUUID()).build())
                .listItems(List.of(DynamicListElement.builder().label("Applicant").code(UUID.randomUUID()).build()))
                .build())
            .statementDocument(Document.builder()
                .filename("statement.exe")
                .url("http://dm/documents/abc")
                .binaryUrl("http://dm/documents/abc/binary")
                .build())
            .build());

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, CASEWORKER_UPLOAD_STATEMENT)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(ABOUT_TO_SUBMIT_VALIDATION_ERROR_RESPONSE)));
    }

    @Test
    void shouldOnlyIncludeAvailablePartiesOnAboutToStart() throws Exception {
        CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder()
                .applicantFullName("Applicant Person")
                .respondentName("")
                .representativeFullName("")
                .build())
            .build();

        String response = mockMvc.perform(post(ABOUT_TO_START_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, CASEWORKER_UPLOAD_STATEMENT)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .inPath("$.data.statementUploadStatementParty.list_items")
            .isArray()
            .hasSize(1);
        assertThatJson(response)
            .inPath("$.data.statementUploadStatementParty.list_items[0].label")
            .isEqualTo("Applicant");
    }

    @Test
    void shouldReturnNoErrorsOnMidEventWhenDocumentIsValid() throws Exception {
        CaseData caseData = caseData();
        caseData.setStatementUpload(StatementUpload.builder()
            .statementDocument(Document.builder()
                .filename("statement.pdf")
                .url("http://dm/documents/abc")
                .binaryUrl("http://dm/documents/abc/binary")
                .build())
            .build());

        String response = mockMvc.perform(post(MID_EVENT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, CASEWORKER_UPLOAD_STATEMENT)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(MID_EVENT_VALID_RESPONSE)));
    }

    @Test
    void shouldReturnValidationErrorOnMidEventWhenDocumentIsInvalid() throws Exception {
        CaseData caseData = caseData();
        caseData.setStatementUpload(StatementUpload.builder()
            .statementDocument(Document.builder()
                .filename("statement.exe")
                .url("http://dm/documents/abc")
                .binaryUrl("http://dm/documents/abc/binary")
                .build())
            .build());

        String response = mockMvc.perform(post(MID_EVENT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, CASEWORKER_UPLOAD_STATEMENT)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(MID_EVENT_INVALID_RESPONSE)));
    }

    @Test
    void shouldReturnConfirmationHeaderOnSubmitted() throws Exception {
        String response = mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData(), CASEWORKER_UPLOAD_STATEMENT)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .isEqualTo("# Statement uploaded");
    }
}
