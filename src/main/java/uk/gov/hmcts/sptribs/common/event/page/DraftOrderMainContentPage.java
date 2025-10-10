package uk.gov.hmcts.sptribs.common.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderContentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class DraftOrderMainContentPage implements CcdPageConfiguration {

    private static final String NEVER_SHOW = "orderContentOrderTemplate=\"NEVER_SHOW\"";
    //    private static final String HIDE_ISSUING_TYPE = "currentEvent=\"create-and-send-order\"";
    private Map<String, String> pageShowCondition;

    public DraftOrderMainContentPage(Map<String, String> pageShowCondition) {
        this.pageShowCondition = pageShowCondition;
    }

    public DraftOrderMainContentPage() {
        this(new HashMap<>());
    }

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("mainContent")
            .pageLabel("Edit order")
            .pageShowConditions(pageShowCondition)
            //            .pageShowConditions(PageShowConditionsUtil.createAndSendOrderConditions())
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
            //            .complex(CaseData::getCicCase)
            //            .readonly(CicCase::getOrderIssuingType)
            .done();
    }
}
