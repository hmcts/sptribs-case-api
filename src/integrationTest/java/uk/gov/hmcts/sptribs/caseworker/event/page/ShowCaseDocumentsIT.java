package uk.gov.hmcts.sptribs.caseworker.event.page;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.sptribs.caseworker.model.DocumentManagement;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.config.WebMvcConfig;
import uk.gov.hmcts.sptribs.testutil.IdamWireMock;

import java.util.ArrayList;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SHOW_CASE_DOCUMENTS_MID_EVENT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCaseworkerCICDocumentList;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {IdamWireMock.PropertiesInitializer.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ShowCaseDocumentsIT {

    private static final String SHOW_CASE_DOCUMENTS_RESPONSE =
        "classpath:responses/show-case-documents-mid-event-response.json";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
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
    void shouldValidateRemoveDocumentSelectedInMidEvent() throws Exception {
        CaseData caseData = CaseData.builder()
            .allDocManagement(DocumentManagement.builder()
                .caseworkerCICDocument(new ArrayList<>())
                .build())
            .cicCase(CicCase.builder()
                .decisionDocumentList(new ArrayList<>())
                .finalDecisionDocumentList(new ArrayList<>())
                .orderDocumentList(new ArrayList<>())
                .build())
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .allDocManagement(DocumentManagement.builder()
                .caseworkerCICDocument(getCaseworkerCICDocumentList())
                .build())
            .cicCase(CicCase.builder()
                .decisionDocumentList(new ArrayList<>())
                .finalDecisionDocumentList(new ArrayList<>())
                .orderDocumentList(new ArrayList<>())
                .build())
            .build();

        mockMvc.perform(post(SHOW_CASE_DOCUMENTS_MID_EVENT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        caseDataBefore,
                        CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andExpect(
                content().json(expectedResponse(SHOW_CASE_DOCUMENTS_RESPONSE))
            );
    }
}
