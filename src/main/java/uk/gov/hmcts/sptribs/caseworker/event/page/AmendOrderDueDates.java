package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.DateModel;
import uk.gov.hmcts.sptribs.caseworker.model.DueDateOptions;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.EMPTY_DATE_MODEL;
import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.MISSING_DUE_DATE;
import static uk.gov.hmcts.sptribs.ciccase.model.GetAmendDateAsCompleted.MARKASCOMPLETED;

public class AmendOrderDueDates implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("caseworkerAmendDueDateEditDueDate", this::midEvent)
            .pageLabel("Amend due dates")
            .list(CaseData::getOrderDueDates)
            .optionalWithLabel(DateModel::getDueDateOptions, "Please choose the updated due date or add a new custom date")
            .optional(DateModel::getUpdatedDueDate, "orderDueDates.dueDateOptions=\"Other\"")
            .readonlyWithLabel(DateModel::getDueDate, "Previous Due Date (This is the previous due date chosen)")
            .optional(DateModel::getOrderMarkAsCompleted);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {

        List<ListValue<DateModel>> dueDates = details.getData().getOrderDueDates();
        final List<String> errors = new ArrayList<>();

        if (isEmpty(dueDates)) {
            errors.add(EMPTY_DATE_MODEL);
            return buildResponse(details, errors);
        }

        dueDates.forEach(dateModelListValue -> processDateModel(dateModelListValue.getValue(), errors));

        return buildResponse(details, errors);
    }

    private void processDateModel(DateModel dateModel, List<String> errors) {
        if (dateModel.getDueDateOptions() == null) {
            dateModel.setDueDateOptions(DueDateOptions.OTHER);
            dateModel.setUpdatedDueDate(dateModel.getDueDate());
        }

        if (dateModel.getDueDateOptions().equals(DueDateOptions.OTHER)
            && dateModel.getUpdatedDueDate() == null) {

            if (dateModel.getOrderMarkAsCompleted().equals(Set.of(MARKASCOMPLETED))) {
                dateModel.setUpdatedDueDate(dateModel.getDueDate());
            } else {
                errors.add(MISSING_DUE_DATE);
            }
        }
    }

    private boolean isEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }

    private AboutToStartOrSubmitResponse<CaseData, State> buildResponse(
        CaseDetails<CaseData, State> details,
        List<String> errors) {

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .errors(errors)
            .build();
    }
}
