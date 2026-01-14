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
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderContentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.config.WebMvcConfig;
import uk.gov.hmcts.sptribs.document.DocAssemblyService;
import uk.gov.hmcts.sptribs.document.model.DocumentInfo;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.testutil.IdamWireMock;

import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CREATE_DRAFT_ORDER;
import static uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate.CIC3_RULE_27;
import static uk.gov.hmcts.sptribs.ciccase.model.SchemeCic.Year2012;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_DATA_CIC_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_DATA_FILE_CIC;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_URL;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getHearingList;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {IdamWireMock.PropertiesInitializer.class})
public class CaseworkerCreateDraftOrderIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WebMvcConfig webMvcConfig;

    @MockitoBean
    private IdamService idamService;

    @MockitoBean
    private DocAssemblyService docAssemblyService;

    private static final String CASEWORKER_CREATE_DRAFT_ORDER_MID_EVENT_RESPONSE =
        "classpath:responses/caseworker-create-draft-order-mid-event-response.json";
    private static final String CASEWORKER_CREATE_DRAFT_ORDER_ABOUT_TO_SUBMIT_RESPONSE =
        "classpath:responses/caseworker-create-draft-order-about-to-submit-response.json";

    public static final String CREATE_DRAFT_ORDER_ADD_FOOTER_MID_EVENT_URL =
        "/callbacks/mid-event?page=createDraftOrderAddDocumentFooter";
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
    void shouldGenerateOrderFileOnMidEvent() throws Exception {
        final CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder()
                .fullName("Test Name")
                .schemeCic(Year2012)
                .build()
            )
            .hearingList(getHearingList())
            .draftOrderContentCIC(DraftOrderContentCIC.builder()
                .orderTemplate(CIC3_RULE_27)
                .orderSignature("Mr Judge")
                .mainContent("Draft Order Content")
                .build()
            )
            .build();

        final User user = new User(
            TEST_AUTHORIZATION_TOKEN,
            UserDetails.builder()
                .roles(List.of("caseworker-st_cic"))
                .build()
        );

        final DocumentInfo documentInfo = new DocumentInfo(
            TEST_URL,
            CASE_DATA_FILE_CIC,
            CASE_DATA_FILE_CIC,
            CASE_DATA_CIC_ID
        );

        when(idamService.retrieveUser(eq(TEST_AUTHORIZATION_TOKEN)))
            .thenReturn(user);

        when(docAssemblyService.renderDocument(
            anyMap(),
            eq(TEST_CASE_ID),
            eq(TEST_AUTHORIZATION_TOKEN),
            eq(CIC3_RULE_27.getId()),
            eq(ENGLISH),
            anyString()
        )).thenReturn(documentInfo);

        String response = mockMvc.perform(post(CREATE_DRAFT_ORDER_ADD_FOOTER_MID_EVENT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CASEWORKER_CREATE_DRAFT_ORDER)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(CASEWORKER_CREATE_DRAFT_ORDER_MID_EVENT_RESPONSE)));

        verify(docAssemblyService).renderDocument(
            anyMap(),
            eq(TEST_CASE_ID),
            eq(TEST_AUTHORIZATION_TOKEN),
            eq(CIC3_RULE_27.getId()),
            eq(ENGLISH),
            anyString()
        );
    }

    @Test
    void shouldUpdateDraftOrderListOnAboutToSubmit() throws Exception {
        final CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder()
                .fullName("Test Name")
                .schemeCic(Year2012)
                .orderTemplateIssued(Document.builder()
                    .categoryId("CIC")
                    .binaryUrl("TestUrl/binary")
                    .filename("SENT :Order--[Subject AutoTesting]--29-05-2024 13:36:27.pdf")
                    .url("TestUrl")
                    .build())
                .build()
            )
            .hearingList(getHearingList())
            .draftOrderContentCIC(DraftOrderContentCIC.builder()
                .orderTemplate(CIC3_RULE_27)
                .orderSignature("Mr Judge")
                .mainContent("Draft Order Content")
                .build()
            )
            .build();

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CASEWORKER_CREATE_DRAFT_ORDER)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(CASEWORKER_CREATE_DRAFT_ORDER_ABOUT_TO_SUBMIT_RESPONSE)));
    }

    @Test
    void shouldReturnConfirmationMessageOnSubmitted() throws Exception {
        String response = mockMvc.perform(post(SUBMITTED_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData(),
                    CASEWORKER_CREATE_DRAFT_ORDER)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Draft order created.");
    }
}
