package uk.gov.hmcts.sptribs.common.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;


@Slf4j
@Component
public class PreviewDraftOrder implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("previewOrdersDocuments")
            .pageLabel("Preview order")
            .label("LabelPreviewOrdersDocuments", "")
            .complex(CaseData::getCicCase)
            .readonly(CicCase::getOrderTemplateIssued)
            .label("make Changes", """
                To make changes, choose ‘Previous’ and navigate back to the Edit Order page.'

                If you are happy, continue to the next screen.""")
            .done();
    }
}

