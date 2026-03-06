package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.sptribs.caseworker.util.DraftRemoveListUtil.setDraftListForRemoval;

public class ShowDraftOrders implements CcdPageConfiguration {


    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("showCaseDocuments", this::midEvent)
            .pageLabel("Show draft documents")
            .label("LabelShowDraftDocuments", "")
            .complex(CaseData::getCicCase)
            .readonlyWithLabel(CicCase::getDraftOrderCICList, "Draft orders")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final CaseData oldData = detailsBefore.getData();
        if (ObjectUtils.isEmpty(data.getCicCase().getRemovedDraftList())) {
            List<ListValue<DraftOrderCIC>> removedDocumentList = new ArrayList<>();
            data.getCicCase().setRemovedDraftList(removedDocumentList);
        }
        final CaseData newCaseData = setDraftListForRemoval(data, oldData);

        final List<String> errors = new ArrayList<>();
        if (CollectionUtils.isEmpty(newCaseData.getCicCase().getRemovedDraftList())) {
            errors.add("Please remove at least one document to continue");
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(newCaseData)
            .errors(errors)
            .build();
    }


}
