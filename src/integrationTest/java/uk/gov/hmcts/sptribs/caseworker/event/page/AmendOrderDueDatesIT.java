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
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.DateModel;
import uk.gov.hmcts.sptribs.caseworker.model.DueDateOptions;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.GetAmendDateAsCompleted;
import uk.gov.hmcts.sptribs.common.config.WebMvcConfig;
import uk.gov.hmcts.sptribs.testutil.IdamWireMock;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.MISSING_DUE_DATE;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_AMEND_DUE_DATE;
import static uk.gov.hmcts.sptribs.ciccase.model.GetAmendDateAsCompleted.MARKASCOMPLETED;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.AMEND_ORDER_DUE_DATES_MID_EVENT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ERRORS;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {IdamWireMock.PropertiesInitializer.class})
public class AmendOrderDueDatesIT {

    private static final String ORDER_DUE_DATES_NO_OPTIONS =
        "classpath:responses/amend-order-due-dates-no-options-mid-event-response.json";
    private static final String ORDER_DUE_DATES_OTHER_OPTION_AND_COMPLETE =
        "classpath:responses/amend-order-due-dates-other-selcted-mid-event-response.json";

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
    void shouldSetUpdatedDueDateCorrectlyWithDueDateOptionsNull() throws Exception {
        DateModel dateModel = DateModel.builder()
            .dueDateOptions(null)
            .dueDate(LocalDate.of(2026, 11, 12))
            .build();

        ListValue<DateModel> listValue = new ListValue<>();
        listValue.setId("1");
        listValue.setValue(dateModel);

        CaseData caseData = CaseData.builder()
            .orderDueDates(List.of(listValue))
            .build();

        mockMvc.perform(post(AMEND_ORDER_DUE_DATES_MID_EVENT_URL)
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
                content().json(expectedResponse(ORDER_DUE_DATES_NO_OPTIONS))
            );

    }

    @Test
    void shouldSetUpdatedDueDateCorrectlyWithOtherSelectedAndOrderCompleted() throws Exception {
        DateModel dateModel = DateModel.builder()
            .dueDateOptions(DueDateOptions.OTHER)
            .orderMarkAsCompleted(Set.of(MARKASCOMPLETED))
            .dueDate(LocalDate.of(2026, 11, 12))
            .build();

        ListValue<DateModel> listValue = new ListValue<>();
        listValue.setId("1");
        listValue.setValue(dateModel);

        CaseData caseData = CaseData.builder()
            .orderDueDates(List.of(listValue))
            .build();

        mockMvc.perform(post(AMEND_ORDER_DUE_DATES_MID_EVENT_URL)
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
                content().json(expectedResponse(ORDER_DUE_DATES_OTHER_OPTION_AND_COMPLETE))
            );
    }

    @Test
    void shouldReturnErrorWithOtherSelectedAndOrderNotCompleted() throws Exception {
        Set<GetAmendDateAsCompleted> getAmendDateAsCompleted = new HashSet<>();

        DateModel dateModel = DateModel.builder()
            .dueDateOptions(DueDateOptions.OTHER)
            .orderMarkAsCompleted(getAmendDateAsCompleted)
            .dueDate(LocalDate.of(2026, 11, 12))
            .build();

        ListValue<DateModel> listValue = new ListValue<>();
        listValue.setId("1");
        listValue.setValue(dateModel);

        CaseData caseData = CaseData.builder()
            .orderDueDates(List.of(listValue))
            .build();

        mockMvc.perform(post(AMEND_ORDER_DUE_DATES_MID_EVENT_URL)
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
                jsonPath(ERRORS)
                    .value(MISSING_DUE_DATE)
            );
    }
}
