package uk.gov.hmcts.sptribs.common.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.List;


@Slf4j
@Component
public class EditDraftOrder implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("editDraftOrder", this::midEvent)
            .pageLabel("Edit order")
            .label("LabelEditDraftOrder", "Draft to be edited")
            .complex(CaseData::getCicCase)
            .optional(CicCase::getDraftOrderDynamicList)
            .done();
    }

    private AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                   CaseDetails<CaseData, State> beforeDetails) {
        CaseData caseData = details.getData();
        DynamicList dynamicList = caseData.getCicCase().getDraftOrderDynamicList();
        String code = dynamicList.getValue().getCode().toString();
        List<ListValue<DraftOrderCIC>> draftOrderCICList = caseData.getCicCase().getDraftOrderCICList();
        draftOrderCICList
            .stream()
            .filter(listValue -> listValue.getValue().getCode().equals(code))
            .findFirst()
            .ifPresent(listValue -> {
                caseData.setDraftOrderContentCIC(listValue.getValue().getDraftOrderContentCIC());
                caseData.getCicCase().setOrderTemplate(listValue.getValue().getTemplate());
            });
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
