package uk.gov.hmcts.sptribs.common.event.page;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.sptribs.document.DocumentUtil.validateCaseworkerCICDocumentFormat;

public class CaseUploadDocuments implements CcdPageConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(CaseUploadDocuments.class);

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("documentsUploadObject", this::midEvent)
            .pageLabel("Upload tribunal forms")
            .label("LabelCaseUploadDocuments",
                "\nPlease upload a copy of the completed tribunal form, as well as any"
                    + " supporting documents or other information that has been supplied.\n"
                    + "\n<h3>Files should be:</h3>\n"
                    + "\n- uploaded separately, and not in one large file\n"
                    + "\n- a maximum of 100MB in size (large files must be split)\n"
                    + "\n- labelled clearly, e.g. applicant-name-B1-form.pdf\n\n")
            .complex(CaseData::getCicCase)
            .optionalWithLabel(CicCase::getApplicantDocumentsUploaded, "File Attachments")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        List<String> errors = new ArrayList<>();
        LOG.info("Start of midEvent");

        List<ListValue<CaseworkerCICDocument>> uploadedDocuments = data.getCicCase().getApplicantDocumentsUploaded();
        errors = validateCaseworkerCICDocumentFormat(uploadedDocuments);

        LOG.info("End of midEvent");
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
}
