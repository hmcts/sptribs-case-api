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
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocumentUpload;

import java.util.List;

import static uk.gov.hmcts.sptribs.document.DocumentUtil.validateUploadedDocuments;

public class CaseUploadDocuments implements CcdPageConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(CaseUploadDocuments.class);

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("documentsUploadObject", this::midEvent)
            .pageLabel("Upload tribunal forms")
            .label("LabelCaseUploadDocuments",
                """
                    Please upload a copy of the completed tribunal form, \
                    as well as any supporting documents or other information that have been supplied
                    Files should be:
                    *  uploaded separately, not one large file
                    *  a maximum of 100MB in size (larger files must be split)
                    *  select the appropriate category from case file view

                    """)
            .complex(CaseData::getCicCase)
            .mandatoryWithLabel(CicCase::getCaseDocumentsUpload, "File Attachments")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();

        List<ListValue<CaseworkerCICDocumentUpload>> uploadedDocuments = data.getCicCase().getCaseDocumentsUpload();
        List<String> errors = validateUploadedDocuments(uploadedDocuments);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
}
