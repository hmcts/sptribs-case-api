package uk.gov.hmcts.sptribs.common.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderContentCIC;
import uk.gov.hmcts.sptribs.caseworker.util.EventUtil;
import uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import static org.apache.commons.lang3.StringUtils.isEmpty;


public class CreateDraftOrder implements CcdPageConfiguration {

    private static final String NEVER_SHOW = "orderContentOrderTemplate=\"NEVER_SHOW\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("createDraftOrder", this::midEvent)
            .pageLabel("Create order")
            .pageShowConditions(PageShowConditionsUtil.createAndSendOrderConditions())
            .label("LabelCreateDraftOrder", "")
            .label("createDraftOrder", "Draft to be created")
            .complex(CaseData::getDraftOrderContentCIC)
                .mandatory(DraftOrderContentCIC::getOrderTemplate)
                .done()
            .readonly(CaseData::getCurrentEvent, NEVER_SHOW)
            .complex(CaseData::getCicCase)
                .readonly(CicCase::getReferralTypeForWA, NEVER_SHOW)
                .readonly(CicCase::getCreateAndSendIssuingTypes, NEVER_SHOW)
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {

        final CaseData caseData = details.getData();
        final OrderTemplate order = caseData.getDraftOrderContentCIC().getOrderTemplate();
        caseData.getDraftOrderContentCIC().setMainContent(EventUtil.getOrderMainContent(order));

        if (isEmpty(caseData.getCicCase().getReferralTypeForWA())) {
            caseData.getCicCase().setReferralTypeForWA("");
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }


}
