package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;

public class IssueCaseSelectDocument implements CcdPageConfiguration {

    private static final int MAX_DOCUMENT_COUNT = 5;

    @Override
    public <T extends CaseData> void addTo(PageBuilder<T> pageBuilder) {

        pageBuilder.page("issueCaseSelectDocument", this::midEvent)
            .pageLabel("Select additional documents")
            .label("LabelIssueCaseSelectDocument", "")
            .label("LabelIssueCaseSelectDocumentWarning", "")
            .complex(CaseData::getCaseIssue)
            .optional(CaseIssue::getDocumentList)
            .done();
    }

    public <T extends CaseData> AboutToStartOrSubmitResponse<T, State> midEvent(CaseDetails<T, State> details,
                                                                  CaseDetails<T, State> detailsBefore) {
        final T data = details.getData();
        final List<String> errors = new ArrayList<>();
        DynamicMultiSelectList list = data.getCaseIssue().getDocumentList();
        if (ObjectUtils.isEmpty(list)
            || CollectionUtils.isEmpty(list.getValue())) {
            errors.add("Select at least one document");
        } else if (list.getValue().size() > MAX_DOCUMENT_COUNT) {
            errors.add("Select up to 5 documents");
        }

        return AboutToStartOrSubmitResponse.<T, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
}
