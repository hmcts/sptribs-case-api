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

        //handle old cases (before radio buttons)
        List<ListValue<DateModel>> dueDates = details.getData().getOrderDueDates();
        final List<String> errors = new ArrayList<>();

        for (ListValue<DateModel> listValue : dueDates) {
            DateModel dateModel = listValue.getValue();

            if (dateModel.getDueDateOptions() == null) {
                dateModel.setDueDateOptions(DueDateOptions.OTHER);
                dateModel.setUpdatedDueDate(dateModel.getDueDate());
            }

            if (dateModel.getDueDateOptions().equals(DueDateOptions.OTHER)
                && dateModel.getUpdatedDueDate() == null) {
                errors.add("Updated due date cannot be null");
            }

        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .errors(errors)
            .build();
    }
}
