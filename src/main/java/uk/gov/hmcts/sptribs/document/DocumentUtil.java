package uk.gov.hmcts.sptribs.document;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocumentUpload;
import uk.gov.hmcts.sptribs.document.model.DocumentInfo;

import java.time.Clock;
import java.time.LocalDate;
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
        if (CollectionUtils.isNotEmpty(documentList)) {
            documentList.forEach(doc -> {
                if (doc != null) {
                    doc.getValue().getDocumentLink()
                        .setCategoryId(doc.getValue().getDocumentCategory().getCategory());
                }
            });
        }
    }

    public static void updateCategoryToDocument(List<ListValue<CICDocument>> documentList, String categoryId) {
        if (CollectionUtils.isNotEmpty(documentList)) {
            documentList.forEach(doc -> {
                if (doc != null) {
                    doc.getValue().getDocumentLink().setCategoryId(categoryId);
                }
            });
        }
    }

    public static List<String> validateDocumentFormat(List<ListValue<CICDocument>> documentList) {
        final List<String> errors = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(documentList)) {
            documentList.stream()
                .filter(value -> value != null && !value.getValue().isDocumentValid())
                .findFirst()
                .ifPresent(x -> errors.add(DOCUMENT_VALIDATION_MESSAGE));
        }

        return errors;
    }

    public static List<String> validateCaseworkerCICDocumentFormat(List<ListValue<CaseworkerCICDocument>> documentList) {
        final List<String> errors = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(documentList)) {
            documentList.stream()
                .filter(value -> value != null && !value.getValue().isDocumentValid())
                .findFirst()
                .ifPresent(x -> errors.add(DOCUMENT_VALIDATION_MESSAGE));
        }

        return errors;
    }

    public static List<String> validateCaseworkerCICDocumentUploadFormat(List<ListValue<CaseworkerCICDocumentUpload>> documentList) {
        final List<String> errors = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(documentList)) {
            documentList.stream()
                .filter(value -> value != null && !value.getValue().isDocumentValid())
                .findFirst()
                .ifPresent(x -> errors.add(DOCUMENT_VALIDATION_MESSAGE));
        }

        return errors;
    }

    public static List<String> validateDecisionDocumentFormat(CICDocument document) {
        final List<String> errors = new ArrayList<>();
        if (document != null && !document.isDocumentValid()) {
            errors.add(DOCUMENT_VALIDATION_MESSAGE);
        }

        return errors;
    }

    public static void uploadDocument(CaseData data) {
        updateCategoryToCaseworkerDocument(data.getNewDocManagement().getCaseworkerCICDocument());
        data.getAllDocManagement().getCaseworkerCICDocument().addAll(data.getNewDocManagement().getCaseworkerCICDocument());
        data.getNewDocManagement().setCaseworkerCICDocument(new ArrayList<>());
    }

    public static List<String> validateUploadedDocuments(List<ListValue<CaseworkerCICDocument>> uploadedDocuments) {
        List<String> errors = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(uploadedDocuments)) {
            for (ListValue<CaseworkerCICDocument> documentListValue : uploadedDocuments) {
                if (ObjectUtils.isEmpty(documentListValue.getValue().getDocumentLink())) {
                    errors.add("Please attach the document");
                } else {
                    errors.addAll(validateCaseworkerCICDocumentFormat(List.of(documentListValue)));

                    if (StringUtils.isEmpty(documentListValue.getValue().getDocumentEmailContent())) {
                        errors.add("Description is mandatory for each document");
                    }
                    if (ObjectUtils.isEmpty(documentListValue.getValue().getDocumentCategory())) {
                        errors.add("Category is mandatory for each document");
                    }
                }
            }
        }

        return errors;
    }

    public static List<String> validateCICUploadedDocuments(List<ListValue<CaseworkerCICDocumentUpload>> uploadedDocuments) {
        List<String> errors = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(uploadedDocuments)) {
            for (ListValue<CaseworkerCICDocumentUpload> documentListValue : uploadedDocuments) {
                if (ObjectUtils.isEmpty(documentListValue.getValue().getDocumentLink())) {
                    errors.add("Please attach the document");
                } else {
                    errors.addAll(validateCaseworkerCICDocumentUploadFormat(List.of(documentListValue)));

                    if (StringUtils.isEmpty(documentListValue.getValue().getDocumentEmailContent())) {
                        errors.add("Description is mandatory for each document");
                    }
                    if (ObjectUtils.isEmpty(documentListValue.getValue().getDocumentCategory())) {
                        errors.add("Category is mandatory for each document");
                    }
                }
            }
        }

        return errors;
    }

    public static List<ListValue<CaseworkerCICDocument>> addDateToUploadedDocuments(List<ListValue<CaseworkerCICDocumentUpload>> uploadedDocuments) {
        Clock clock = Clock.systemDefaultZone();

        List<ListValue<CaseworkerCICDocument>> documentList = new ArrayList<>();
        if (uploadedDocuments != null && !uploadedDocuments.isEmpty()) {
            uploadedDocuments.forEach(
                listValue -> {
                    CaseworkerCICDocumentUpload uploadedDocument = listValue.getValue();
                    CaseworkerCICDocument document = CaseworkerCICDocument.builder()
                        .documentCategory(uploadedDocument.getDocumentCategory())
                        .documentEmailContent(uploadedDocument.getDocumentEmailContent())
                        .documentLink(uploadedDocument.getDocumentLink())
                        .date(LocalDate.now(clock))
                        .build();

                    ListValue<CaseworkerCICDocument> documentListValue = new ListValue<>();
                    documentListValue.setId(listValue.getId());
                    documentListValue.setValue(document);

                    documentList.add(documentListValue);
                }
            );
        }

        return documentList;
    }

    public static List<ListValue<CaseworkerCICDocumentUpload>> removeDateFromUploadedDocuments(List<ListValue<CaseworkerCICDocument>> uploadedDocuments) {
        List<ListValue<CaseworkerCICDocumentUpload>> documentList = new ArrayList<>();
        if (uploadedDocuments != null && !uploadedDocuments.isEmpty()) {
            uploadedDocuments.forEach(
                listValue -> {
                    CaseworkerCICDocument uploadedDocument = listValue.getValue();
                    CaseworkerCICDocumentUpload document = CaseworkerCICDocumentUpload.builder()
                        .documentCategory(uploadedDocument.getDocumentCategory())
                        .documentEmailContent(uploadedDocument.getDocumentEmailContent())
                        .documentLink(uploadedDocument.getDocumentLink())
                        .build();

                    ListValue<CaseworkerCICDocumentUpload> documentListValue = new ListValue<>();
                    documentListValue.setId(listValue.getId());
                    documentListValue.setValue(document);

                    documentList.add(documentListValue);
                }
            );
        }

        return documentList;
    }

}
