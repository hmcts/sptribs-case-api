package uk.gov.hmcts.sptribs.common.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderContentCIC;
import uk.gov.hmcts.sptribs.caseworker.util.DynamicListUtil;
import uk.gov.hmcts.sptribs.caseworker.util.EventUtil;
import uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class CreateNewOrder implements CcdPageConfiguration {

    public static final String HIDDEN = "LabelCreateNewOrder=\"HIDDEN\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
                .page("createNewOrder", this::midEvent)
                .pageLabel("Create order")
                .pageShowConditions(PageShowConditionsUtil.createAndSendOrderConditions())
                .label("LabelCreateNewOrder", "")
                .label("createDraftOrder", "Draft to be created")
                .complex(CaseData::getDraftOrderContentCIC)
                    .readonly(DraftOrderContentCIC::getOrderTemplate, HIDDEN)
                    .done()
                .complex(CaseData::getCicCase)
                    .mandatory(CicCase::getTemplateDynamicList)
                    .readonly(CicCase::getReferralTypeForWA, HIDDEN)
                    .done()
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {

        final CaseData caseData = details.getData();
        final CicCase cicCase = caseData.getCicCase();

        if (cicCase.getTemplateDynamicList() != null) {
            OrderTemplate selectedValue = DynamicListUtil.getEnumFromUuid(
                cicCase.getTemplateDynamicList().getValueCode(),
                OrderTemplate.class);
            DraftOrderContentCIC draftOrderContentCIC = DraftOrderContentCIC.builder()
                .orderTemplate(selectedValue)
                .mainContent(EventUtil.getOrderMainContent(selectedValue))
                .build();
            caseData.setDraftOrderContentCIC(draftOrderContentCIC);
        }

        if (isEmpty(caseData.getCicCase().getReferralTypeForWA())) {
            caseData.getCicCase().setReferralTypeForWA("");
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .build();
    }

}
