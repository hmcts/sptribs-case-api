package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.DateModel;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class AmendOrderDueDates implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("caseworkerAmendDueDateEditDueDate")
            .pageLabel("Amend due dates")
            .list(CaseData::getOrderDueDates)
            .optionalWithLabel(DateModel::getDueDateOptions, "Please choose the updated due date or add a new custom date")
            .optional(DateModel::getUpdatedDueDate, "orderDueDates.dueDateOptions=\"Other\"")
            .readonlyWithLabel(DateModel::getDueDate, "Previous Due Date (This is the previous due date chosen)")
            .optional(DateModel::getOrderMarkAsCompleted);
    }
}
