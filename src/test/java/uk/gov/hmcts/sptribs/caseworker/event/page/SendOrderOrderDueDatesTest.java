package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.DateModel;
import uk.gov.hmcts.sptribs.caseworker.model.DueDateOptions;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendOrderOrderDueDatesTest {

    @Mock
    private DueDateOptions dueDateOptions;

    private final Clock fixedClock = Clock.fixed(
        LocalDate.of(2026, 7, 15)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant(),
        ZoneId.systemDefault()
    );

    private SendOrderOrderDueDates sendOrderOrderDueDates;

    @BeforeEach
    void setUp() {
        sendOrderOrderDueDates =
            new SendOrderOrderDueDates(fixedClock);
    }

    @Test
    void givenDateModel_whenMidEvent_thenCalculateDateFrom21DaysInput() {
        //given
        final CaseDetails<CaseData, State> caseDetails = buildCaseDetails(List.of(21L));

        when(dueDateOptions.getAmount()).thenReturn(21L);

        //when
        AboutToStartOrSubmitResponse<CaseData, State> response = sendOrderOrderDueDates.midEvent(caseDetails, caseDetails);

        //then
        CaseData actualCaseData = response.getData();
        assertEquals(1, actualCaseData.getOrderDueDates().size(), "assert list of dates is of length 1");
        assertEquals(LocalDate.now(fixedClock).plusDays(21L),
            actualCaseData.getOrderDueDates().getFirst().getValue().getDueDate(), "assert the dates are the same");
        assertNull(response.getErrors());

    }

    @Test
    void givenDateModel_whenMidEvent_thenCalculateDates() {
        //given
        final CaseDetails<CaseData, State> caseDetails = buildCaseDetails(Arrays.asList(21L, 120L, null));

        when(dueDateOptions.getAmount())
            .thenReturn(21L, 120L, null);

        //when
        AboutToStartOrSubmitResponse<CaseData, State> response = sendOrderOrderDueDates.midEvent(caseDetails, caseDetails);

        //then
        LocalDate today = LocalDate.now(fixedClock);
        CaseData actualCaseData = response.getData();
        List<ListValue<DateModel>> dates = actualCaseData.getOrderDueDates();
        assertEquals(3, actualCaseData.getOrderDueDates().size(), "assert list of dates is of length 3");
        assertEquals(today.plusDays(21), dates.get(0).getValue().getDueDate(), "dates are the same");
        assertEquals(today.plusDays(120), dates.get(1).getValue().getDueDate(), "dates are the same ");
        assertEquals(LocalDate.of(2026, 5, 5),
            dates.get(2).getValue().getDueDate(), "assert the dates are the same for Other");
        assertNull(response.getErrors());

    }

    private CaseDetails<CaseData, State> buildCaseDetails(List<Long> daysSelected) {

        List<ListValue<DateModel>> dateModels = new ArrayList<>();

        for (Long selectedDay : daysSelected) {
            ListValue<DateModel> dateModelListValue = new ListValue<>();
            LocalDate inputtedLocalDate;

            if (selectedDay == null) {
                inputtedLocalDate = LocalDate.of(2026, 5, 5);
            } else {
                inputtedLocalDate = null;
            }

            DateModel dateModel = DateModel.builder()
                .dueDateOptions(dueDateOptions)
                .dueDate(inputtedLocalDate)
                .build();

            dateModelListValue.setId("123");
            dateModelListValue.setValue(dateModel);

            dateModels.add(dateModelListValue);

        }


        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = CaseData.builder()
            .orderDueDates(dateModels)
            .build();
        caseDetails.setData(caseData);

        return caseDetails;
    }

}
