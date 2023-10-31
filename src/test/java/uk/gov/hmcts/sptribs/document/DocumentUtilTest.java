package uk.gov.hmcts.sptribs.document;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.DocumentManagement;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentInfo;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.DOCUMENT_VALIDATION_MESSAGE;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.documentFrom;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.updateCategoryToCaseworkerDocument;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.updateCategoryToDocument;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCICDocumentList;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCICDocumentListWithInvalidFileFormat;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCaseworkerCICDocumentList;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCaseworkerCICDocumentListWithFileFormat;

@ExtendWith(MockitoExtension.class)
class DocumentUtilTest {

    private static final String DOC_URL = "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003";
    private static final String DOC_BINARY_URL = "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003/binary";
    private static final String PDF_FILENAME = "draft-divorce-application-1616591401473378.pdf";
    private static final String CATEGORY_ID_VAL = "A";
    private static final String URL = "url";
    private static final String FILENAME = "filename";
    private static final String BINARY_URL = "binaryUrl";
    private static final String CATEGORY_ID = "categoryId";

    @Test
    void shouldConvertFromDocumentInfoToDocument() {
        //When
        final Document document = documentFrom(documentInfo());

        //Then
        assertThat(document)
            .extracting(URL, FILENAME, BINARY_URL, CATEGORY_ID)
            .contains(
                DOC_URL,
                PDF_FILENAME,
                DOC_BINARY_URL,
                CATEGORY_ID_VAL);
    }

    @Test
    void shouldValidateCICDocumentFormat() {
        //When
        List<ListValue<CICDocument>> documentList = getCICDocumentListWithInvalidFileFormat();
        List<String> errors = DocumentUtil.validateDocumentFormat(documentList);

        //Then
        assertThat(errors).contains(DOCUMENT_VALIDATION_MESSAGE);
    }

    @Test
    void shouldValidateCaseworkerCICDocumentFormatValid() {
        //When
        CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().url("url1").binaryUrl("url1").filename("name1").build())
            .build();
        List<String> errors = DocumentUtil.validateCICDocumentFormat(doc);

        //Then
        assertThat(errors).hasSize(1);
    }

    @Test
    void shouldValidateOneCaseworkerCICDocumentFormatValid() {
        //When
        List<ListValue<CaseworkerCICDocument>> documentList = getCaseworkerCICDocumentListWithFileFormat("docx");
        List<String> errors = DocumentUtil.validateCaseworkerCICDocumentFormat(documentList);

        //Then
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldValidateAllChecksCaseworkerCICDocumentFormatValid() {
        //When
        List<ListValue<CaseworkerCICDocument>> documentList = getCaseworkerCICDocumentListWithFileFormat("docx");
        List<String> errors = DocumentUtil.validateUploadedDocuments(documentList);

        //Then
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldValidateAllChecksCaseworkerCICDocumentFormatValidEmptyDocument() {
        //When
        List<ListValue<CaseworkerCICDocument>> documentList = new ArrayList<>();
        final CaseworkerCICDocument document = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentEmailContent("some email content")
            .build();
        ListValue<CaseworkerCICDocument> documentListValue = new ListValue<>();
        documentListValue.setValue(document);
        documentList.add(documentListValue);
        List<String> errors = DocumentUtil.validateUploadedDocuments(documentList);

        //Then
        assertThat(errors).hasSize(1);
    }

    @Test
    void shouldValidateAllChecksCaseworkerCICDocumentFormatValidEmptyDesc() {
        //When
        List<ListValue<CaseworkerCICDocument>> documentList = new ArrayList<>();
        final CaseworkerCICDocument document = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder().filename("file.pdf").build())
            .build();
        ListValue<CaseworkerCICDocument> documentListValue = new ListValue<>();
        documentListValue.setValue(document);
        documentList.add(documentListValue);
        List<String> errors = DocumentUtil.validateUploadedDocuments(documentList);

        //Then
        assertThat(errors).hasSize(1);
    }

    @Test
    void shouldValidateAllChecksCaseworkerCICDocumentFormatValidEmptyDescEmptyCategory() {
        //When
        List<ListValue<CaseworkerCICDocument>> documentList = new ArrayList<>();
        final CaseworkerCICDocument document = CaseworkerCICDocument.builder()
            .documentLink(Document.builder().filename("file.pdf").build())
            .build();
        ListValue<CaseworkerCICDocument> documentListValue = new ListValue<>();
        documentListValue.setValue(document);
        documentList.add(documentListValue);
        List<String> errors = DocumentUtil.validateUploadedDocuments(documentList);

        //Then
        assertThat(errors).hasSize(2);
    }

    @Test
    void shouldValidateCaseworkerCICDocumentFormatInvalid() {
        //When
        List<ListValue<CaseworkerCICDocument>> documentList = getCaseworkerCICDocumentListWithFileFormat("xml");
        List<String> errors = DocumentUtil.validateCaseworkerCICDocumentFormat(documentList);

        //Then
        assertThat(errors).contains(DOCUMENT_VALIDATION_MESSAGE);
    }

    @Test
    void shouldValidateDecisionDocumentFormat() {
        //When
        final CICDocument document = CICDocument.builder()
            .documentLink(Document.builder().filename("file.txt").build())
            .documentEmailContent("some email content")
            .build();

        List<String> errors = DocumentUtil.validateDecisionDocumentFormat(document);

        //Then
        assertThat(errors).contains(DOCUMENT_VALIDATION_MESSAGE);
    }

    @Test
    void shouldUpdateCategoryToCaseworkerDocument() {
        //When
        List<ListValue<CaseworkerCICDocument>> documentList = getCaseworkerCICDocumentListWithFileFormat("docx");
        updateCategoryToCaseworkerDocument(documentList);

        //Then
        assertThat(documentList.get(0).getValue().getDocumentLink().getCategoryId()).isEqualTo("L");
    }

    @Test
    void shouldUpdateCategoryToCaseworkerDocumentNull() {
        //When
        List<ListValue<CaseworkerCICDocument>> documentList = null;

        //Then
        assertDoesNotThrow(() -> updateCategoryToCaseworkerDocument(documentList));
    }

    @Test
    void shouldUpdateCategoryToDocument() {
        //When
        List<ListValue<CICDocument>> documentList = getCICDocumentList();
        String categoryId = "1";
        updateCategoryToDocument(documentList, categoryId);

        //Then
        assertThat(documentList.get(0).getValue().getDocumentLink().getCategoryId()).isEqualTo(categoryId);
    }

    @Test
    void shouldUpdateCategoryToDocumentNull() {
        //When
        List<ListValue<CICDocument>> documentList = null;
        String categoryId = "1";

        //Then
        assertDoesNotThrow(() -> updateCategoryToDocument(documentList, categoryId));
    }

    @Test
    void shouldSuccessfullyAddDocument() {
        //Given
        final CaseData caseData = caseData();
        DocumentManagement documentManagement = DocumentManagement.builder()
            .caseworkerCICDocument(getCaseworkerCICDocumentList())
            .build();
        caseData.setAllDocManagement(documentManagement);

        //When
        DocumentUtil.uploadDocument(caseData);

        //Then
        assertThat(caseData.getAllDocManagement().getCaseworkerCICDocument()).hasSize(1);
    }

    private DocumentInfo documentInfo() {
        return new DocumentInfo(
            DOC_URL,
            PDF_FILENAME,
            DOC_BINARY_URL,
            CATEGORY_ID_VAL
        );
    }

}
