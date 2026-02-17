package uk.gov.hmcts.sptribs.caseworker.event.page;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.DateModel;
import uk.gov.hmcts.sptribs.caseworker.model.DueDateOptions;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.config.WebMvcConfig;
import uk.gov.hmcts.sptribs.testutil.IdamWireMock;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CREATE_AND_SEND_ORDER;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.*;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.sptribs.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {IdamWireMock.PropertiesInitializer.class})
public class SendOrderOrderDueDatesIT {


    private static final String ORDER_DUE_DATES_120_DAYS =
        "classpath:responses/order-due-dates-120-days-mid-event-response.json";
    private static final String ORDER_DUE_DATES_OTHER =
        "classpath:responses/order-due-dates-other-mid-event-response.json";

    @TestConfiguration
    static class FixedClockConfig {

        @Bean
        public Clock clock() {
            return Clock.fixed(
                LocalDate.of(2026, 7, 15)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant(),
                ZoneId.systemDefault()
            );
        }
    }

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
    void shouldSetDueDateOnMidEventFor120Days() throws Exception {

        DateModel dateModel = DateModel.builder()
            .dueDateOptions(DueDateOptions.DAY_COUNT_120)
            .dueDate(LocalDate.of(2026, 11, 12))
            .build();

        ListValue<DateModel> listValue = new ListValue<>();
        listValue.setId("1");
        listValue.setValue(dateModel);

        CaseData caseData = CaseData.builder()
            .orderDueDates(List.of(listValue))
            .build();

        mockMvc.perform(post(ORDER_DUE_DATES_MID_EVENT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_CREATE_AND_SEND_ORDER)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andExpect(
                content().json(expectedResponse(ORDER_DUE_DATES_120_DAYS))
            );

    }

    @Test
    void shouldSetDueDateOnMidEventForOther() throws Exception {

        DateModel dateModel = DateModel.builder()
            .dueDateOptions(DueDateOptions.OTHER)
            .dueDate(LocalDate.of(2026, 9, 4))
            .build();

        ListValue<DateModel> listValue = new ListValue<>();
        listValue.setId("1");
        listValue.setValue(dateModel);

        CaseData caseData = CaseData.builder()
            .orderDueDates(List.of(listValue))
            .build();

        mockMvc.perform(post(ORDER_DUE_DATES_MID_EVENT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_CREATE_AND_SEND_ORDER)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andExpect(
                content().json(expectedResponse(ORDER_DUE_DATES_OTHER))
            );

    }

}
