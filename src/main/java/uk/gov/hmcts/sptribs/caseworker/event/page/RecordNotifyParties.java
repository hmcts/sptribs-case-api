package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;

public class RecordNotifyParties implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("notifyPage")
            .label("notifyLabel", "<h1>Notify page</h1>")
            .complex(CaseData::getCicCase)
            .label("notifyLabelRecListing", "Which parties would be notified of this listing?")
            .readonlyWithLabel(CicCase::getFullName, " ")
            .optional(CicCase::getRecordNotifyPartySubject, "cicCaseFullName!=\"\" ")
            .label("notifyLabelRecordApplicant", "")
            .readonlyWithLabel(CicCase::getRepresentativeFullName, " ")
            .optional(CicCase::getRecordNotifyPartyRepresentative, "cicCaseRepresentativeFullName!=\"\" ")
            .label("notifyLabelRecordRep", "")
            .readonlyWithLabel(CicCase::getRespondantName, " ")
            .optional(CicCase::getRecordNotifyPartyRespondent, "cicCaseRespondantName!=\"\" ")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEventPage(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final List<String> errors = new ArrayList<>();

        if (checkNullCondition(details.getData().getCicCase())) {
            errors.add("One party must be selected.");
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .errors(errors)
            .build();
    }

    private boolean checkNullCondition(CicCase cicCase) {
        return null != cicCase
            && CollectionUtils.isEmpty(cicCase.getRecordNotifyPartySubject())
            && CollectionUtils.isEmpty(cicCase.getRecordNotifyPartyRepresentative())
            && CollectionUtils.isEmpty(cicCase.getRecordNotifyPartyRespondent());
    }
}
