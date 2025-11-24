package uk.gov.hmcts.sptribs.common.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import static uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil.ORDER_EVENT_CREATE_AND_SEND_NEW;
import static uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil.ORDER_EVENT_CREATE_AND_SEND_UPLOAD;
import static uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil.ORDER_EVENT_CREATE_DRAFT;
import static uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil.ORDER_EVENT_EDIT_DRAFT;


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
            .readonly(CicCase::getOrderTemplateIssued, ORDER_EVENT_CREATE_DRAFT + " OR " + ORDER_EVENT_EDIT_DRAFT
                    + " OR (" + ORDER_EVENT_CREATE_AND_SEND_NEW + ")")
            .label("make Changes", """
                To make changes, choose ‘Previous’ and navigate back to the Edit Order page.'

                If you are happy, continue to the next screen.""", ORDER_EVENT_CREATE_DRAFT + " OR " + ORDER_EVENT_EDIT_DRAFT
                    + " OR (" + ORDER_EVENT_CREATE_AND_SEND_NEW + ")")
            .done()
            .complex(CaseData::getCicCase)
            .readonly(CicCase::getOrderFile, ORDER_EVENT_CREATE_AND_SEND_UPLOAD)
            .done();
    }
}

