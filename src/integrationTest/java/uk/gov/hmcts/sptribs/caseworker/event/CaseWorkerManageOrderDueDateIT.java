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
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.DateModel;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.config.WebMvcConfig;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.testutil.IdamWireMock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCICDocumentList;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_AMEND_DUE_DATE;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {IdamWireMock.PropertiesInitializer.class})
public class CaseWorkerManageOrderDueDateIT {

    private static final String CASEWORKER_MANAGE_ORDER_DUE_DATE_ABOUT_TO_START_RESPONSE =
        "classpath:responses/caseworker-manage-order-due-date-about-to-start-response.json";
    private static final String CASEWORKER_MANAGE_ORDER_DUE_DATE_ABOUT_TO_SUBMIT_RESPONSE =
        "classpath:responses/caseworker-manage-order-due-date-about-to-submit-response.json";
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
    void shouldSetOrderDynamicListInAboutToStart() throws Exception {
        List<ListValue<CICDocument>> documentList = getCICDocumentList("filename");

        Order order = Order.builder()
            .uploadedFile(documentList)
            .build();

        ListValue<Order> orderListValue = new ListValue<>();
        orderListValue.setValue(order);

        List<ListValue<Order>> list = new ArrayList<>();
        list.add(orderListValue);

        CicCase cicCase = CicCase.builder()
            .orderList(list)
            .build();

        CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();

        mockMvc.perform(post(ABOUT_TO_START_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_AMEND_DUE_DATE)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andExpect(
                content().json(expectedResponse(CASEWORKER_MANAGE_ORDER_DUE_DATE_ABOUT_TO_START_RESPONSE))
            );
    }

    @Test
    void shouldSetDueDateOnSelectedOrderInAboutToSubmit() throws Exception {
        List<ListValue<CICDocument>> documentList = getCICDocumentList("filename");

        Order order = Order.builder()
            .uploadedFile(documentList)
            .build();

        final DynamicListElement listItem = DynamicListElement.builder()
            .label("testId")
            .code(UUID.randomUUID())
            .build();
        final DynamicList dynamicList = DynamicList.builder()
            .value(listItem)
            .listItems(List.of(listItem))
            .build();

        ListValue<Order> orderListValue = new ListValue<>();
        orderListValue.setId("testId");
        orderListValue.setValue(order);

        List<ListValue<Order>> list = new ArrayList<>();
        list.add(orderListValue);

        DateModel orderDueDate = DateModel.builder()
            .dueDate(LocalDate.of(2024, 5, 11))
            .information("information")
            .build();

        ListValue<DateModel> dueDateListValue = new ListValue<>();
        dueDateListValue.setValue(orderDueDate);

        List<ListValue<DateModel>> dueDateList = new ArrayList<>();
        dueDateList.add(dueDateListValue);

        CicCase cicCase = CicCase.builder()
            .orderList(list)
            .orderDynamicList(dynamicList)
            .orderDueDates(dueDateList)
            .build();

        CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_AMEND_DUE_DATE)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andExpect(
                content().json(expectedResponse(CASEWORKER_MANAGE_ORDER_DUE_DATE_ABOUT_TO_SUBMIT_RESPONSE))
            );
    }

    @Test
    void shouldReturnSuccessMessageInSubmitted() throws Exception {
        final CaseData caseData = caseData();

        String response = mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_AMEND_DUE_DATE)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .inPath(CONFIRMATION_HEADER)
            .isString()
            .contains("# Due dates amended.");
    }
}
