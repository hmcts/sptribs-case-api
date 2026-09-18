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
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.config.WebMvcConfig;
import uk.gov.hmcts.sptribs.common.repositories.DocumentsRepository;
import uk.gov.hmcts.sptribs.document.bundling.model.Bundle;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentType;
import uk.gov.hmcts.sptribs.document.service.DocumentsService;
import uk.gov.hmcts.sptribs.testutil.IdamWireMock;

import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.ASYNC_STITCH_COMPLETE;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {IdamWireMock.PropertiesInitializer.class})
public class CaseworkerBundleStitchCompleteIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WebMvcConfig webMvcConfig;

    @MockitoBean
    private DocumentsService documentsService;

    @MockitoBean
    private DocumentsRepository documentsRepository;

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
    void shouldSaveStitchedBundleAndReturnSuccessfulResponse() throws Exception {
        CaseData caseData = setCaseDataWithTestCaseBundles();

        String response = mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        ASYNC_STITCH_COMPLETE)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Documents added successfully");
    }

    @Test
    void shouldReturnUnsuccessfulResponseForNullStitchedBundle() throws Exception {
        CaseData caseData = caseData();
        Bundle testBundle1 = Bundle.builder().stitchedDocument(null).build();
        List<ListValue<Bundle>> testCaseBundles =
            List.of(
                ListValue.<Bundle>builder()
                    .id("1")
                    .value(testBundle1)
                    .build());
        caseData.setCaseBundles(testCaseBundles);

        String response = mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        ASYNC_STITCH_COMPLETE)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# No stitched bundle document found");
    }

    @Test
    void shouldReturnUnsuccessfulResponseForExceptionsThrownSavingBundles() throws Exception {
        CaseData caseData = caseData();

        Document testStitchedDocumentWithoutBinaryUrl = Document.builder()
            .build();
        Bundle testBundle1 = Bundle.builder().stitchedDocument(testStitchedDocumentWithoutBinaryUrl).build();

        List<ListValue<Bundle>> testCaseBundles =
            List.of(
                ListValue.<Bundle>builder()
                    .id("1")
                    .value(testBundle1)
                    .build());
        caseData.setCaseBundles(testCaseBundles);

        doThrow(new RuntimeException("Error saving document entity to database"))
            .when(documentsService).buildAndSaveNewDocumentEntity(eq(caseData.getCaseBundles().getFirst().getValue().getStitchedDocument()),
                eq(TEST_CASE_ID), eq(null), eq(CaseDocumentType.BUNDLE));

        String response = mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        ASYNC_STITCH_COMPLETE)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Error saving latest case bundle to document entity");
    }

    private CaseData setCaseDataWithTestCaseBundles() {
        CaseData caseData = caseData();

        Document testStitchedDocument1 = Document.builder()
            .filename("stitched-document-1.pdf")
            .url("http://test.com/a1b2c3d4")
            .binaryUrl("http://test.com/a1b2c3d4/binary")
            .build();
        Bundle testBundle1 = Bundle.builder().stitchedDocument(testStitchedDocument1).build();

        Document testStitchedDocument2 = Document.builder()
            .filename("stitched-document-2.pdf")
            .url("http://test.com/e5f6g7h8")
            .binaryUrl("http://test.com/e5f6g7h8/binary")
            .build();
        Bundle testBundle2 = Bundle.builder().stitchedDocument(testStitchedDocument2).build();

        Document testStitchedDocument3 = Document.builder()
            .filename("stitched-document-3.pdf")
            .url("http://test.com/i9j0k1l2")
            .binaryUrl("http://test.com/i9j0k1l2/binary")
            .build();
        Bundle testBundle3 = Bundle.builder().stitchedDocument(testStitchedDocument3).build();

        List<ListValue<Bundle>> testCaseBundles =
            List.of(
                ListValue.<Bundle>builder()
                    .id("1")
                    .value(testBundle1)
                    .build(),
                ListValue.<Bundle>builder()
                    .id("2")
                    .value(testBundle2)
                    .build(),
                ListValue.<Bundle>builder()
                    .id("3")
                    .value(testBundle3)
                    .build()
            );
        caseData.setCaseBundles(testCaseBundles);
        return caseData;
    }
}
