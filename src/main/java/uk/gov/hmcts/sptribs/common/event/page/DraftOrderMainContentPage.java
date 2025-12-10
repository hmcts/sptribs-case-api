package uk.gov.hmcts.sptribs.common.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderContentCIC;
import uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants;


@Slf4j
@Component
public class DraftOrderMainContentPage implements CcdPageConfiguration {

    private static final String NEVER_SHOW = "orderContentOrderTemplate=\"NEVER_SHOW\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("mainContent", this::midEvent)
            .pageLabel("Edit order")
            .pageShowConditions(PageShowConditionsUtil.createAndSendOrderConditions())
            .label("EditDraftOrderMainContent", """
                <hr>
                <h3>Header</h3>
                The header will be automatically generated. You can preview this in the pdf document on the next screen.

                <hr>
                <h3>Main content</h3>

                Enter text in the box below. This will be added into the centre of the generated order document.
                """)
            .complex(CaseData::getDraftOrderContentCIC)
                .mandatory(DraftOrderContentCIC::getMainContent)
                .done()
            .label("footer", """
                <h3>Footer</h3>
                 The footer will be automatically generated.
                 You can preview this in the pdf document on the next screen.
                <hr>
                """)
            .readonly(CaseData::getCurrentEvent, NEVER_SHOW)
            .done();
    }

    private AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> caseDetails,
                                                                   CaseDetails<CaseData, State> caseDetailsBefore) {
        CaseData caseData = caseDetails.getData();
        CicCase cicCase = caseData.getCicCase();
        StringBuilder orderMainContent = new StringBuilder(caseData.getDraftOrderContentCIC().getMainContent());
        if (cicCase.getAnonymiseYesOrNo() != null && YesOrNo.YES.equals(cicCase.getAnonymiseYesOrNo())
                && cicCase.getAnonymisedAppellantName() != null && cicCase.getAnonymisationDate() != null) {
            orderMainContent.append(DocmosisTemplateConstants.generateAnonymisationStatement(cicCase.getAnonymisationDate()));
        }
        caseData.getDraftOrderContentCIC().setMainContent(orderMainContent.toString());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .build();
    }
}
