package uk.gov.hmcts.sptribs.document;

import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentInfo;

import java.util.ArrayList;
import java.util.List;

public final class DocumentUtil {

    private DocumentUtil() {
    }

    public static Document documentFrom(final DocumentInfo documentInfo) {
        return new Document(
            documentInfo.getUrl(),
            documentInfo.getFilename(),
            documentInfo.getBinaryUrl(),
            documentInfo.getCategoryId()
        );
    }

    public static void updateCategoryToDocument(List<ListValue<CICDocument>> documentList) {
        documentList.stream().forEach(doc -> doc.getValue().getDocumentLink().setCategoryId("A"));
    }

    public static List<String> validateDocumentFormat(List<ListValue<CICDocument>> documentList) {
        final List<String> errors = new ArrayList<>();
        if (!documentList.isEmpty() && !documentList.get(0).getValue().isDocumentValid()) {
            errors.add("Please upload valid document");
        }

        return errors;
    }

    public static List<String> validateDecisionDocumentFormat(CICDocument document) {
        final List<String> errors = new ArrayList<>();
        if (null != document && !document.isDocumentValid()) {
            errors.add("Please upload valid document");
        }

        return errors;
    }

}
