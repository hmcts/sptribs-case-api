package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.DateModel;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.time.LocalDate;
import java.util.List;

public class SendOrderOrderDueDates implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("caseworkerSendOrderOrderDueDates", this::midEvent)
            .pageLabel("Add a due date")
            .list(CaseData::getOrderDueDates)
            .mandatory(DateModel::getDueDateOptions)
            .optional(DateModel::getDueDate, "orderDueDates.dueDateOptions=\"Other\"")
            .optional(DateModel::getOrderMarkAsCompleted)
            .done()
            .complex(CaseData::getCicCase)
            .optional(CicCase::getAdminActionRequired)
            .readonly(CicCase::getFirstOrderDueDate, "orderDueDates=\"HIDDEN\"");

    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {

        List<ListValue<DateModel>> dueDates = details.getData().getOrderDueDates();
        for (ListValue<DateModel> listValue : dueDates) {

            Long dueDateOffset = listValue.getValue().getDueDateOptions().getAmount();

            //if not other
            if (dueDateOffset != null) {
                listValue.getValue().setDueDate((LocalDate.now().plusDays(dueDateOffset)));
            }

        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }

}
