package uk.gov.hmcts.sptribs.common.event.page;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CREATE_AND_SEND_ORDER;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CREATE_DRAFT_ORDER;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_EDIT_DRAFT_ORDER;

@Slf4j
public class PreviewDraftOrder implements CcdPageConfiguration {

    public static final String SHOW_CHANGE_DRAFT = "cicCaseOrderIssuingType=\"\" OR cicCaseOrderIssuingType=\"NewOrder\"";
    public static final String SHOW_CHANGE_UPLOAD = "cicCaseOrderIssuingType=\"UploadOrder\"";

    private final String pageId;
    private final String eventId;

    public PreviewDraftOrder(String pageId, String eventId) {
        this.pageId = pageId;
        this.eventId = eventId;
    }

    @Override
    public void addTo(PageBuilder pageBuilder) {
        FieldCollection.FieldCollectionBuilder<CaseData, State, Event.EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder =
            pageBuilder
                .page(pageId)
                .pageLabel("Preview order")
                .label("LabelPreviewOrdersDocuments", "");

        if (eventId.equals(CASEWORKER_CREATE_DRAFT_ORDER) || eventId.equals(CASEWORKER_EDIT_DRAFT_ORDER)) {
            fieldCollectionBuilder.complex(CaseData::getCicCase)
                .readonly(CicCase::getOrderTemplateIssued)
                .label("make Changes", """
                    To make changes, choose ‘Previous’ and navigate back to the 'Edit Order' page.
                    
                    If you are happy, continue to the next screen.""")
                .done();
        } else if (eventId.equals(CASEWORKER_CREATE_AND_SEND_ORDER)) {
            fieldCollectionBuilder
                .complex(CaseData::getCicCase)
                .readonly(CicCase::getOrderTemplateIssued)
                .label("make Changes", """
                    To make changes, choose ‘Previous’ and navigate back to the 'Edit Order' page.
                    
                    If you are happy, continue to the next screen.""", SHOW_CHANGE_DRAFT)
                .label("changeUpload", """
                    To change the document, choose ‘Previous’ and navigate back to the 'Upload an Order' page.
                    
                    If you are happy, continue to the next screen.""", SHOW_CHANGE_UPLOAD)
                .done();
        }
    }
}

