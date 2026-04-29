package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.*;
import uk.gov.hmcts.ccd.sdk.api.callback.*;
import uk.gov.hmcts.sptribs.caseworker.model.*;
import uk.gov.hmcts.sptribs.ciccase.model.*;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.model.*;

import java.util.*;

import static uk.gov.hmcts.sptribs.document.DocumentUtil.validateHearingStatementCICDocumentFormat;


public class UploadHearingStatementDocument implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("uploadHearingStatementDocument", this::midEvent)
            .pageLabel("Upload Hearing Statement documents")
            .label("LabelUploadHearingStatementDocuments", "")
            .label("uploadHearingStatementDocumentLabel", "Upload copy of the document you wish to be added to case file view")
            .label("uploadAcknowledgementDocumentConditions", """
                The order should be:
                 *  uploaded separately, not one large file
                 *  a maximum of 100MB in size (larger files must be split)
                 *  labelled clearly, e.g. applicant-name-decision-notice.pdf




                 Note: If the remove button is disabled, please refresh the page to remove attachments
                """
            )
            .complex(CaseData::getNewDocManagement)
            .mandatory(DocumentManagement::getHearingStatementCICDocument)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();

        HearingStatementCICDocument uploadedDocument = data.getNewDocManagement().getHearingStatementCICDocument();
        List<String> errors = validateHearingStatementCICDocumentFormat(uploadedDocument);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
}
