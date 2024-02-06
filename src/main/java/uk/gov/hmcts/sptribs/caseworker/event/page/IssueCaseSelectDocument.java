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
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("issueCaseSelectDocument", this::midEvent)
            .pageLabel("Select additional documents")
            .label("LabelIssueCaseSelectDocument", "")
            .label("LabelIssueCaseSelectDocumentWarning", "")
            .complex(CaseData::getCaseIssue)
            .optional(CaseIssue::getDocumentList)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();
        DynamicMultiSelectList list = data.getCaseIssue().getDocumentList();
        if (ObjectUtils.isEmpty(list)
            || CollectionUtils.isEmpty(list.getValue())) {
            errors.add("Select at least one document");
        } else if (list.getValue().size() > MAX_DOCUMENT_COUNT) {
            errors.add("Select up to 5 documents");
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
}
