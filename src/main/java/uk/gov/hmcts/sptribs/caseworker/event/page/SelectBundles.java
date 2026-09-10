package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;

public class SelectBundles implements CcdPageConfiguration {

    @Override
    public <T extends CaseData> void addTo(PageBuilder<T> pageBuilder) {
        pageBuilder.page("selectBundles", this::midEvent)
            .pageLabel("Select bundles to delete")
            .complex(CaseData::getCicCase)
            .optional(CicCase::getRemoveBundlesList)
            .done();
    }

    public <T extends CaseData> AboutToStartOrSubmitResponse<T, State> midEvent(CaseDetails<T, State> details,
                                                                  CaseDetails<T, State> beforeDetails) {
        final T data = details.getData();
        final List<String> errors = new ArrayList<>();

        List<DynamicListElement> selectedBundlesToRemove = data.getCicCase().getRemoveBundlesList().getValue();
        if (selectedBundlesToRemove.isEmpty()) {
            errors.add("Select at least one bundle to remove");
        }

        return AboutToStartOrSubmitResponse.<T, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
}
