package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.HYPHEN;


public class SendOrderAddDraftOrder implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        String pageNameUploadOrder = "caseworkerSendOrderUploadOrder";
        String pageNameDraftOrder = "caseworkerSendOrderSelectDraftOrder";
        Map<String, String> map = new HashMap<>();
        map.put(pageNameDraftOrder, "cicCaseOrderIssuingType = \"DraftOrder\"");
        map.put(pageNameUploadOrder, "cicCaseOrderIssuingType = \"UploadOrder\"");
        pageBuilder.page(pageNameDraftOrder, this::midEvent)
            .pageLabel("Select order")
            .pageShowConditions(map)
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getDraftList)
            .readonlyNoSummary(CicCase::getDraftOrderCICList)
            .label("errorMessageNoDraftOrder", "<h2>There are no draft orders on this case</h2>", "cicCaseDraftOrderCICList =\"\"")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        String selectedDraft = data.getCicCase().getDraftList().getValue().getLabel();
        String id = getId(selectedDraft);
        var draftList = data.getCicCase().getDraftOrderCICList();
        for (int i = 0; i < draftList.size(); i++) {
            if (null != id && Integer.parseInt(id) == i) {
                data.getCicCase().setDraftOrderCIC(draftList.get(i).getValue());
            }
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }

    private String getId(String selectedDraft) {
        String[] values = (selectedDraft != null) ? Arrays.stream(selectedDraft.split(HYPHEN))
            .map(String::trim)
            .toArray(String[]::new) : null;
        return values != null && values.length > 0 ? values[0] : null;
    }
}
