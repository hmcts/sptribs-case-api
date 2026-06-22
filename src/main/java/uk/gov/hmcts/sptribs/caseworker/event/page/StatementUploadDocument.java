package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.statement.model.StatementUpload;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.sptribs.document.DocumentConstants.DOCUMENT_VALIDATION_MESSAGE;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.isValidDocument;

@Component
public class StatementUploadDocument implements CcdPageConfiguration {

    private static final String VALID_DOCUMENT_EXTENSIONS =
        "pdf,csv,txt,rtf,xlsx,docx,doc,xls,mp3,m4a,mp4,jpg,jpeg,bmp,tif,tiff,png";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("statementUploadDocument", this::midEvent)
            .pageLabel("Upload statement document")
            .complex(CaseData::getStatementUpload)
            .mandatory(StatementUpload::getStatementDocument)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                   CaseDetails<CaseData, State> detailsBefore) {
        CaseData caseData = details.getData();
        List<String> errors = new ArrayList<>();
        StatementUpload statementUpload = caseData.getStatementUpload();

        if (statementUpload != null) {
            Document statementDocument = statementUpload.getStatementDocument();
            if (statementDocument != null
                && (StringUtils.isAnyBlank(statementDocument.getFilename(), statementDocument.getUrl(),
                statementDocument.getBinaryUrl())
                || !isValidDocument(statementDocument.getFilename(), VALID_DOCUMENT_EXTENSIONS))) {
                errors.add(DOCUMENT_VALIDATION_MESSAGE);
            }
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .errors(errors)
            .build();
    }
}
