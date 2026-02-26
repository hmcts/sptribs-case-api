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
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderContentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate;
import uk.gov.hmcts.sptribs.common.config.WebMvcConfig;
import uk.gov.hmcts.sptribs.testutil.IdamWireMock;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_DELETE_DRAFT_ORDER;
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
public class CaseworkerDeleteDraftOrderIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WebMvcConfig webMvcConfig;


    private static final String CONFIRMATION_HEADER = "$.confirmation_header";


    private static final String CASEWORKER_DELETE_DRAFT_ORDER_REPOPULATE_DYNAMIC_LIST =
        "classpath:responses/caseworker-delete-draft-order-repopulate-dynamic-list-response.json";


    @BeforeAll
    static void setUp() {
        IdamWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        IdamWireMock.stopAndReset();
    }

    public CaseData buildTestCaseData() {
        final CaseData caseData = caseData();

        final Document document = Document.builder()
            .url("test/documents/a57d1138-1f8d-4aeb-b5ad-3681aba68747")
            .filename("Order--[test]--24-02-2026 15:47:25.pdf")
            .binaryUrl("test")
            .categoryId("TD")
            .build();

        final DraftOrderContentCIC content = DraftOrderContentCIC.builder()
            .orderTemplate(OrderTemplate.CIC3_RULE_27)
            .mainContent("test")
            .orderSignature("test")
            .build();

        final DraftOrderCIC draftOrder = DraftOrderCIC.builder()
            .templateGeneratedDocument(document)
            .draftOrderContentCIC(content)
            .build();

        final ListValue<DraftOrderCIC> lv = ListValue.<DraftOrderCIC>builder()
            .id("1")
            .value(draftOrder)
            .build();

        final CicCase cicCase = CicCase.builder()
            .draftOrderCICList(new ArrayList<>(List.of(lv)))
            .removedDraftList(new ArrayList<>(List.of(lv)))
            .draftOrderDynamicList(DynamicList.builder()
                .listItems(new ArrayList<>())
                .build())
            .build();

        caseData.setCicCase(cicCase);

        return caseData;
    }


    @Test
    void shouldClearRemovedDraftListOnAboutToSubmit() throws Exception {
        final CaseData caseData = buildTestCaseData();

        caseData.getCicCase().setRemovedDraftList(caseData.getCicCase().getRemovedDraftList());

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CASEWORKER_DELETE_DRAFT_ORDER)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .inPath("$.data.cicCaseRemovedDraftList")
            .isArray()
            .isEmpty();
    }

    @Test
    void shouldCorrectlyRepopulateDraftListOnAboutToSubmit() throws Exception {
        final CaseData caseData = buildTestCaseData();

        final DynamicListElement listItem = DynamicListElement.builder()
            .label("CIC3 - Rule 27--09-05-2024 09:04:04.pdf--draft.pdf")
            .code(UUID.randomUUID())
            .build();

        caseData.getCicCase().getDraftOrderDynamicList().getListItems().add(listItem);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CASEWORKER_DELETE_DRAFT_ORDER)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andExpect(
            status().isOk())
            .andExpect(
                content().json(expectedResponse(CASEWORKER_DELETE_DRAFT_ORDER_REPOPULATE_DYNAMIC_LIST)));
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
                    CASEWORKER_DELETE_DRAFT_ORDER)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Draft order deleted.");
    }
}
