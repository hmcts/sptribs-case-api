package uk.gov.hmcts.sptribs.common.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class PreviewDraftOrder implements CcdPageConfiguration {
    private Map<String, String> pageShowConditions;

    @Autowired
    public PreviewDraftOrder(Map<String, String> pageShowConditions) {
        this.pageShowConditions = pageShowConditions;
    }

    public PreviewDraftOrder() {
        this(new HashMap<>());
    }

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("previewOrdersDocuments")
            .pageLabel("Preview order")
            .pageShowConditions(pageShowConditions)
            //            .pageShowConditions(PageShowConditionsUtil.createAndSendOrderConditions())
            .label("LabelPreviewOrdersDocuments", "")
            .complex(CaseData::getCicCase)
            .readonly(CicCase::getOrderTemplateIssued)
            .label("make Changes", """
                To make changes, choose ‘Previous’ and navigate back to the Edit Order page.'

                If you are happy, continue to the next screen.""")
            .done();
    }
}

