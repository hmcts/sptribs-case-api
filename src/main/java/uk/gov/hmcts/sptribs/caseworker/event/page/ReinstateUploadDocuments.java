package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.apache.commons.lang.StringUtils;
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

import static uk.gov.hmcts.sptribs.document.DocumentUtil.updateCategoryToCaseworkerDocument;

public class ReinstateUploadDocuments implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("reinstateUploadDocument", this::midEvent)
            .pageLabel("Upload documents")
            .label("LabelReinstateCaseUploadDocument", "")
            .complex(CaseData::getCicCase)
            .label("reinstateUploadMessage",
                "<b>Please upload any documents that explain why this case is being reinstated. (Optional)</b>")
            .label("reinstateUploadAdvice", """
                Files should be
                  *  uploaded separately and not in one large file
                  *  a maximum of 100MB in size (larger files must be split)
                  *  labelled clearly, e.g. applicant-name-B1-form.pdf

                Add a file
                Upload a file to the system
                """)
            .optionalWithLabel(CicCase::getReinstateDocuments, "Reinstate Documents")
            .done();

    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();
        List<ListValue<CaseworkerCICDocument>> documents = data.getCicCase().getReinstateDocuments();

        if (null != documents) {
            updateCategoryToCaseworkerDocument(documents);
            for (ListValue<CaseworkerCICDocument> documentListValue : data.getCicCase().getReinstateDocuments()) {
                if (null != documentListValue.getValue().getDocumentLink()
                    && StringUtils.isEmpty(documentListValue.getValue().getDocumentEmailContent())) {
                    errors.add("Description is mandatory for each document");
                }
            }
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
}
