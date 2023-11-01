package uk.gov.hmcts.sptribs.document;

import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentInfo;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.sptribs.document.DocumentConstants.DOCUMENT_VALIDATION_MESSAGE;

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

    public static void updateCategoryToCaseworkerDocument(List<ListValue<CaseworkerCICDocument>> documentList) {
        documentList.forEach(doc -> doc.getValue().getDocumentLink()
            .setCategoryId(doc.getValue().getDocumentCategory().getCategory()));
    }

    public static void updateCategoryToDocument(List<ListValue<CICDocument>> documentList, String categoryId) {
        documentList.forEach(doc -> doc.getValue().getDocumentLink().setCategoryId(categoryId));
    }

    public static List<String> validateDocumentFormat(List<ListValue<CICDocument>> documentList) {
        final List<String> errors = new ArrayList<>();
        if (!documentList.isEmpty() && !documentList.get(0).getValue().isDocumentValid()) {
            errors.add(DOCUMENT_VALIDATION_MESSAGE);
        }

        return errors;
    }

    public static List<String> validateCaseworkerCICDocumentFormat(List<ListValue<CaseworkerCICDocument>> documentList) {
        final List<String> errors = new ArrayList<>();
        if (documentList != null) {
            documentList.stream()
                .filter(value -> !value.getValue().isDocumentValid())
                .findFirst()
                .ifPresent(x -> errors.add(DOCUMENT_VALIDATION_MESSAGE));
        }

        return errors;
    }

    public static List<String> validateDecisionDocumentFormat(CICDocument document) {
        final List<String> errors = new ArrayList<>();
        if (null != document && !document.isDocumentValid()) {
            errors.add(DOCUMENT_VALIDATION_MESSAGE);
        }

        return errors;
    }

}
