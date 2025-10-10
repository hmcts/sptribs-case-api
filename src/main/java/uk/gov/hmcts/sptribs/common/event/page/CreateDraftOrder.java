package uk.gov.hmcts.sptribs.common.event.page;

import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.HashMap;
import java.util.Map;

import static net.logstash.logback.util.StringUtils.isEmpty;

public class CreateDraftOrder implements CcdPageConfiguration {

    private static final String NEVER_SHOW = "orderContentOrderTemplate=\"NEVER_SHOW\"";
    private Map<String, String> pageShowConditions;

    @Autowired
    public CreateDraftOrder(Map<String, String> pageShowConditions) {
        this.pageShowConditions = pageShowConditions;
    }

    public CreateDraftOrder() {
        this(new HashMap<>());
    }

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("createDraftOrder", this::midEvent)
            .pageLabel("Create order")
            .pageShowConditions(pageShowConditions)
            //            .pageShowConditions(PageShowConditionsUtil.createAndSendOrderConditions())
            .label("LabelCreateDraftOrder", "")
            .label("createDraftOrder", "Draft to be created")
            .complex(CaseData::getDraftOrderContentCIC)
                .mandatory(DraftOrderContentCIC::getOrderTemplate)
                .done()
            .readonly(CaseData::getCurrentEvent, NEVER_SHOW)
            .complex(CaseData::getCicCase)
                .readonly(CicCase::getReferralTypeForWA, NEVER_SHOW)
                //                .readonly(CicCase::getOrderIssuingType)
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
