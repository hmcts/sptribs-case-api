package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

import java.util.List;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.DOUBLE_HYPHEN;

public class DocumentManagementSelectDocuments implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("selectCaseDocuments", this::midEvent)
            .pageLabel("Select documents")
            .label("LabelSelectCaseDocuments", "")
            .label("LabelSelectCaseDocumentsWarning", "")
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getAmendDocumentList)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();

        setSelectedDocuments(data);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }

    private void setSelectedDocuments(CaseData data) {
        var cicCase = data.getCicCase();
        DynamicList documentList = cicCase.getAmendDocumentList();
        List<ListValue<CaseworkerCICDocument>> allCaseDocuments = DocumentListUtil.getAllCaseDocuments(data);
        DocumentType selectedDocumentCategory = null;
        String selectedDocumentEmailContent = null;
        Document selectedDocumentLink = null;
        String selectedDocumentType = null;

        if (!ObjectUtils.isEmpty(documentList.getValue())) {
            String selectedDocumentURL = documentList.getValue().getLabel();

            for (ListValue<CaseworkerCICDocument> documentListValue : allCaseDocuments) {
                String[] labels = selectedDocumentURL.split(DOUBLE_HYPHEN);
                String documentTypeLabel = documentListValue.getValue().getDocumentCategory().getLabel();
                String filename = documentListValue.getValue().getDocumentLink().getFilename();
                if (ArrayUtils.isNotEmpty(labels) && labels[1].equals(filename) && labels[2].equals(documentTypeLabel)) {
                    selectedDocumentCategory = documentListValue.getValue().getDocumentCategory();
                    selectedDocumentEmailContent = documentListValue.getValue().getDocumentEmailContent();
                    selectedDocumentLink = documentListValue.getValue().getDocumentLink();
                    selectedDocumentType = labels[0];
                }
            }

            cicCase.setSelectedDocumentCategory(selectedDocumentCategory);
            cicCase.setSelectedDocumentEmailContent(selectedDocumentEmailContent);
            cicCase.setSelectedDocumentLink(selectedDocumentLink);
            cicCase.setSelectedDocumentType(selectedDocumentType);
        }
    }
}
