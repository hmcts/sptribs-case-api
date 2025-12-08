package uk.gov.hmcts.sptribs.common.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderContentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.UUID;
import java.util.stream.IntStream;


@Slf4j
@Component
public class EditDraftOrder implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "cicCaseDraftOrderDynamicList = \"ALWAYS_HIDE\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("editDraftOrder", this::midEvent)
            .pageLabel("Edit order")
            .label("LabelEditDraftOrder", "Draft to be edited")
            .readonly(CaseData::getCurrentEvent, ALWAYS_HIDE)
            .complex(CaseData::getCicCase)
            .mandatoryWithLabel(CicCase::getDraftOrderDynamicList, "Draft order to be edited")
                .readonly(CicCase::getCreateAndSendIssuingTypes, ALWAYS_HIDE)
                .done()
            .complex(CaseData::getDraftOrderContentCIC)
            // readonly below is required in order to be able to set the order template in midEvent below
            .readonly(DraftOrderContentCIC::getOrderTemplate, ALWAYS_HIDE)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                   CaseDetails<CaseData, State> beforeDetails) {
        CaseData caseData = details.getData();
        DynamicList dynamicList = caseData.getCicCase().getDraftOrderDynamicList();
        int listSize = dynamicList.getListItems().size();
        UUID code = dynamicList.getValue().getCode();
        IntStream.range(0, listSize)
            .filter(i -> code.equals(dynamicList.getListItems().get(i).getCode()))
            .findFirst()
            .ifPresent(index -> caseData.setDraftOrderContentCIC(
                // draftOrderCICList is in reverse order
                caseData.getCicCase().getDraftOrderCICList().get(listSize - 1 - index).getValue().getDraftOrderContentCIC()));
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
