package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.DocumentFileTypes;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.sptribs.document.DocumentConstants.DOCUMENT_VALIDATION_MESSAGE;

public class StatementUploadDocument implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("caseworkerStatementUploadDocument", this::midEvent)
            .pageLabel("Upload the statement")
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getUploadPartyStatementDocument);
    }

    private AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> caseDataStateCaseDetails, CaseDetails<CaseData, State> caseDataStateCaseDetails1) {

        // Validate the uploaded document
        Document document = caseDataStateCaseDetails.getData().getCicCase().getUploadPartyStatementDocument();
        final List<String> errors = new ArrayList<>();
        if (document == null || !DocumentFileTypes.isValid(document.getFilename(), DocumentFileTypes.CASEWORKER_UPLOAD_EXTENSIONS)) {
            errors.add(DOCUMENT_VALIDATION_MESSAGE);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseDataStateCaseDetails.getData())
            .errors(errors)
            .build();
    }
}
