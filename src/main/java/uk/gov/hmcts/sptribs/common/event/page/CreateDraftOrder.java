package uk.gov.hmcts.sptribs.common.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderContentCIC;
import uk.gov.hmcts.sptribs.caseworker.util.EventUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class CreateDraftOrder implements CcdPageConfiguration {

    private static final String NEVER_SHOW = "orderContentOrderTemplate=\"NEVER_SHOW\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("createDraftOrder", this::midEvent)
            .pageLabel("Create order")
            .label("LabelCreateDraftOrder", "")
            .label("createDraftOrder", "Draft to be created")
            .complex(CaseData::getDraftOrderContentCIC)
            .mandatory(DraftOrderContentCIC::getOrderTemplate)
            .done()
            .complex(CaseData::getCicCase)
            .readonly(CicCase::getReferralTypeForWA, NEVER_SHOW);
    }


    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {
        CaseData caseData = details.getData();
        OrderTemplate order = caseData.getDraftOrderContentCIC().getOrderTemplate();
        caseData.getDraftOrderContentCIC().setMainContent(EventUtil.getOrderMainContent(order));

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }


}
