package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;

public class FlagAdditionalInfo implements CcdPageConfiguration {

    private static final int FLAG_ADDITIONAL_DETAIL_LIMIT = 200;

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("caseWorkerFlagAdditionalInfo", this::midEvent)
            .pageLabel("Add comments for this flag (Optional)")
            .label("LabelCaseWorkerFlagAdditionalInfo", "")
            .complex(CaseData::getCicCase)
            .optional(CicCase::getFlagAdditionalDetail)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();
        if (data.getCicCase().getFlagAdditionalDetail().length() > FLAG_ADDITIONAL_DETAIL_LIMIT) {
            errors.add("You can enter up to 200 characters");
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
}
