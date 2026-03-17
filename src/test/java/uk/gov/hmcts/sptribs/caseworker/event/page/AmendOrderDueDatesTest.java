package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.DateModel;
import uk.gov.hmcts.sptribs.caseworker.model.DueDateOptions;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.GetAmendDateAsCompleted;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.camunda.bpm.model.xml.test.assertions.ModelAssertions.assertThat;
import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.MISSING_DUE_DATE;
import static uk.gov.hmcts.sptribs.ciccase.model.GetAmendDateAsCompleted.MARKASCOMPLETED;

@ExtendWith(MockitoExtension.class)
class AmendOrderDueDatesTest {

    @InjectMocks
    private AmendOrderDueDates amendOrderDueDates;

    @Test
    void whenDueDateOptionsNull_thenSetUpdatedDueDateAsPrevious() {
        //given
        List<ListValue<DateModel>> dateModels = new ArrayList<>();

        DateModel dateModel = DateModel.builder()
            .dueDateOptions(null)
            .dueDate(LocalDate.of(2026, 5, 5))
            .build();

        ListValue<DateModel> dateModelListValue = new ListValue<>();
        dateModelListValue.setId("123");
        dateModelListValue.setValue(dateModel);

        dateModels.add(dateModelListValue);

        CaseDetails<CaseData, State> caseDetails = buildCaseDetails(dateModels);

        //when
        AboutToStartOrSubmitResponse<CaseData, State> response = amendOrderDueDates.midEvent(caseDetails, caseDetails);

        //then

        CaseData actualCaseData = response.getData();
        DateModel actualDateModel = actualCaseData.getOrderDueDates().getFirst().getValue();
        assertThat(actualDateModel.getDueDateOptions()).isEqualTo(DueDateOptions.OTHER);
        assertThat(actualDateModel.getUpdatedDueDate()).isEqualTo(dateModel.getDueDate());
        assertThat(response.getErrors()).isEmpty();

    }

    @Test
    void whenOtherSelectedButNoDateInputtedAndOrderCompleted_thenSetUpdatedDueDateAsPrevious() {
        //given
        List<ListValue<DateModel>> dateModels = new ArrayList<>();

        DateModel dateModel = DateModel.builder()
            .dueDateOptions(DueDateOptions.OTHER)
            .dueDate(LocalDate.of(2026, 5, 5))
            .orderMarkAsCompleted(Set.of(MARKASCOMPLETED))
            .build();

        ListValue<DateModel> dateModelListValue = new ListValue<>();
        dateModelListValue.setId("123");
        dateModelListValue.setValue(dateModel);

        dateModels.add(dateModelListValue);

        CaseDetails<CaseData, State> caseDetails = buildCaseDetails(dateModels);

        //when
        AboutToStartOrSubmitResponse<CaseData, State> response = amendOrderDueDates.midEvent(caseDetails, caseDetails);

        //then

        CaseData actualCaseData = response.getData();
        DateModel actualDateModel = actualCaseData.getOrderDueDates().getFirst().getValue();
        assertThat(actualDateModel.getDueDateOptions()).isEqualTo(DueDateOptions.OTHER);
        assertThat(actualDateModel.getUpdatedDueDate()).isEqualTo(dateModel.getDueDate());
        assertThat(response.getErrors()).isEmpty();

    }

    @Test
    void whenOtherSelectedButNoDateInputtedAndOrderNotCompleted_thenAddErrorToResponse() {
        //given
        List<ListValue<DateModel>> dateModels = new ArrayList<>();
        Set<GetAmendDateAsCompleted> getAmendDateAsCompleted = new HashSet<>();

        DateModel dateModel = DateModel.builder()
            .dueDateOptions(DueDateOptions.OTHER)
            .dueDate(LocalDate.of(2026, 5, 5))
            .orderMarkAsCompleted(getAmendDateAsCompleted)
            .build();

        ListValue<DateModel> dateModelListValue = new ListValue<>();
        dateModelListValue.setId("123");
        dateModelListValue.setValue(dateModel);

        dateModels.add(dateModelListValue);

        CaseDetails<CaseData, State> caseDetails = buildCaseDetails(dateModels);

        //when
        AboutToStartOrSubmitResponse<CaseData, State> response = amendOrderDueDates.midEvent(caseDetails, caseDetails);

        //then
        assertThat(response.getErrors().getFirst()).isEqualTo(MISSING_DUE_DATE);

    }

    @Test
    void whenOtherNotSelected_thenDataIsAsInputted() {
        //given
        List<ListValue<DateModel>> dateModels = new ArrayList<>();
        Set<GetAmendDateAsCompleted> getAmendDateAsCompleted = new HashSet<>();

        DateModel dateModel = DateModel.builder()
            .dueDateOptions(DueDateOptions.DAY_COUNT_120)
            .dueDate(LocalDate.of(2026, 5, 5))
            .orderMarkAsCompleted(getAmendDateAsCompleted)
            .build();

        ListValue<DateModel> dateModelListValue = new ListValue<>();
        dateModelListValue.setId("123");
        dateModelListValue.setValue(dateModel);

        dateModels.add(dateModelListValue);

        CaseDetails<CaseData, State> caseDetails = buildCaseDetails(dateModels);

        //when
        AboutToStartOrSubmitResponse<CaseData, State> response = amendOrderDueDates.midEvent(caseDetails, caseDetails);

        //then

        CaseData actualCaseData = response.getData();
        DateModel actualDateModel = actualCaseData.getOrderDueDates().getFirst().getValue();
        assertThat(actualDateModel.getDueDateOptions()).isEqualTo(DueDateOptions.DAY_COUNT_120);
        assertThat(response.getErrors()).isEmpty();
    }


    private CaseDetails<CaseData, State> buildCaseDetails(List<ListValue<DateModel>> dateModels) {

        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();

        CaseData caseData = CaseData.builder()
            .orderDueDates(dateModels)
            .build();
        caseDetails.setData(caseData);

        return caseDetails;
    }

}
