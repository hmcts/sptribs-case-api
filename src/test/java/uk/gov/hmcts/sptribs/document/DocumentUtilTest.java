package uk.gov.hmcts.sptribs.document;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.DocumentManagement;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocumentUpload;
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
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCICDocument;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCICDocumentList;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCaseworkerCICDocument;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCaseworkerCICDocumentList;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCaseworkerCICDocumentUpload;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCaseworkerCICDocumentUploadList;

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

    private static final String JPG_FILE = "file.jpg";
    private static final String PDF_FILE = "file.pdf";
    private static final String DOCX_FILE = "file.docx";
    private static final String INVALID_FILE = "file.xyz";
    private static final String EMAIL_CONTENT = "Test Email Content";

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
    void shouldNotUpdateCategoryWhenCaseworkerDocumentIsNull() {
        //Given
        List<ListValue<CaseworkerCICDocument>> documentList = null;

        //Then
        assertDoesNotThrow(() -> updateCategoryToCaseworkerDocument(documentList));
    }

    @Test
    void shouldNotUpdateCategoryWhenCaseworkerDocumentIsEmpty() {
        //Given
        List<ListValue<CaseworkerCICDocument>> documentList = new ArrayList<>();

        //Then
        assertDoesNotThrow(() -> updateCategoryToCaseworkerDocument(documentList));
        assertThat(documentList).hasSize(0);
    }

    @Test
    void shouldNotUpdateCategoryWhenCaseworkerCICDocumentIsNull() {
        //Given
        List<ListValue<CaseworkerCICDocument>> documentList = new ArrayList<>();
        documentList.add(null);

        //Then
        assertDoesNotThrow(() -> updateCategoryToCaseworkerDocument(documentList));
    }

    @Test
    void shouldUpdateCategoryToCaseworkerDocument() {
        //Given
        List<ListValue<CaseworkerCICDocument>> documentList = getCaseworkerCICDocumentList(DOCX_FILE);

        //When
        updateCategoryToCaseworkerDocument(documentList);

        //Then
        assertThat(documentList.get(0).getValue().getDocumentLink().getCategoryId()).isEqualTo(DocumentType.LINKED_DOCS.getCategory());
    }

    @Test
    void shouldUpdateCategoryToCaseworkerDocuments() {
        //Given
        ListValue<CaseworkerCICDocument> document1 = getCaseworkerCICDocument(DOCX_FILE);
        ListValue<CaseworkerCICDocument> document2 = getCaseworkerCICDocument(PDF_FILE);
        List<ListValue<CaseworkerCICDocument>> documentList = new ArrayList<>();
        documentList.add(document1);
        documentList.add(null);
        documentList.add(document2);

        //When
        updateCategoryToCaseworkerDocument(documentList);

        //Then
        assertThat(documentList.get(0).getValue().getDocumentLink().getCategoryId()).isEqualTo(DocumentType.LINKED_DOCS.getCategory());
        assertThat(documentList.get(1)).isNull();
        assertThat(documentList.get(2).getValue().getDocumentLink().getCategoryId()).isEqualTo(DocumentType.LINKED_DOCS.getCategory());
    }

    @Test
    void shouldNotUpdateCategoryWhenDocumentIsNull() {
        //Given
        List<ListValue<CICDocument>> documentList = null;

        //Then
        assertDoesNotThrow(() -> updateCategoryToDocument(documentList, CATEGORY_ID));
    }

    @Test
    void shouldNotUpdateCategoryWhenDocumentIsEmpty() {
        //Given
        List<ListValue<CICDocument>> documentList = new ArrayList<>();

        //Then
        assertDoesNotThrow(() -> updateCategoryToDocument(documentList, CATEGORY_ID));
        assertThat(documentList).hasSize(0);
    }

    @Test
    void shouldNotUpdateCategoryWhenCICDocumentIsNull() {
        //Given
        List<ListValue<CICDocument>> documentList = new ArrayList<>();
        documentList.add(null);

        //Then
        assertDoesNotThrow(() -> updateCategoryToDocument(documentList, CATEGORY_ID));
    }

    @Test
    void shouldUpdateCategoryToDocument() {
        //Given
        List<ListValue<CICDocument>> documentList = getCICDocumentList(PDF_FILE);

        //When
        updateCategoryToDocument(documentList, CATEGORY_ID);

        //Then
        assertThat(documentList.get(0).getValue().getDocumentLink().getCategoryId()).isEqualTo(CATEGORY_ID);
    }

    @Test
    void shouldUpdateCategoryToDocuments() {
        //Given
        ListValue<CICDocument> document1 = getCICDocument(PDF_FILE);
        ListValue<CICDocument> document2 = getCICDocument(JPG_FILE);
        List<ListValue<CICDocument>> documentList = new ArrayList<>();
        documentList.add(document1);
        documentList.add(null);
        documentList.add(document2);

        //When
        updateCategoryToDocument(documentList, CATEGORY_ID);

        //Then
        assertThat(documentList.get(0).getValue().getDocumentLink().getCategoryId()).isEqualTo(CATEGORY_ID);
        assertThat(documentList.get(1)).isNull();
        assertThat(documentList.get(2).getValue().getDocumentLink().getCategoryId()).isEqualTo(CATEGORY_ID);
    }

    @Test
    void shouldNotReturnErrorsWhenDocumentListIsNull() {
        //Given
        List<String> errors = DocumentUtil.validateDocumentFormat(null);

        //Then
        assertThat(errors).hasSize(0);
    }

    @Test
    void shouldNotReturnErrorsWhenDocumentListIsEmpty() {
        //Given
        List<String> errors = DocumentUtil.validateDocumentFormat(new ArrayList<>());

        //Then
        assertThat(errors).hasSize(0);
    }

    @Test
    void shouldReturnErrorsWhenDocumentIsNull() {
        //Given
        ListValue<CICDocument> document = getCICDocument(INVALID_FILE);
        List<ListValue<CICDocument>> documentList = new ArrayList<>();
        documentList.add(document);
        documentList.add(null);

        //When
        List<String> errors = DocumentUtil.validateDocumentFormat(documentList);

        //Then
        assertThat(errors).hasSize(1);
        assertThat(errors).contains(DOCUMENT_VALIDATION_MESSAGE);
    }

    @Test
    void shouldNotReturnErrorsForValidDocument() {
        //Given
        List<ListValue<CICDocument>> documentList = getCICDocumentList(PDF_FILE);

        //When
        List<String> errors = DocumentUtil.validateDocumentFormat(documentList);

        //Then
        assertThat(errors).hasSize(0);
    }

    @Test
    void shouldNotReturnErrorsForValidDocuments() {
        //Given
        ListValue<CICDocument> document1 = getCICDocument(PDF_FILE);
        ListValue<CICDocument> document2 = getCICDocument(JPG_FILE);
        List<ListValue<CICDocument>> documentList = new ArrayList<>();
        documentList.add(document1);
        documentList.add(document2);

        //When
        List<String> errors = DocumentUtil.validateDocumentFormat(documentList);

        //Then
        assertThat(errors).hasSize(0);
    }

    @Test
    void shouldReturnErrorsForInvalidDocument() {
        //Given
        List<ListValue<CICDocument>> documentList = getCICDocumentList(INVALID_FILE);

        //When
        List<String> errors = DocumentUtil.validateDocumentFormat(documentList);

        //Then
        assertThat(errors).hasSize(1);
        assertThat(errors).contains(DOCUMENT_VALIDATION_MESSAGE);
    }

    @Test
    void shouldReturnErrorsForInvalidDocuments() {
        //Given
        ListValue<CICDocument> document1 = getCICDocument(PDF_FILE);
        ListValue<CICDocument> document2 = getCICDocument(INVALID_FILE);
        List<ListValue<CICDocument>> documentList = new ArrayList<>();
        documentList.add(document1);
        documentList.add(document2);

        //When
        List<String> errors = DocumentUtil.validateDocumentFormat(documentList);

        //Then
        assertThat(errors).hasSize(1);
        assertThat(errors).contains(DOCUMENT_VALIDATION_MESSAGE);
    }

    @Test
    void shouldNotReturnErrorsWhenCaseworkerCICDocumentListIsNull() {
        //Given
        List<String> errors = DocumentUtil.validateCaseworkerCICDocumentFormat(null);

        //Then
        assertThat(errors).hasSize(0);
    }

    @Test
    void shouldNotReturnErrorsWhenCaseworkerCICDocumentListIsEmpty() {
        //Given
        List<String> errors = DocumentUtil.validateCaseworkerCICDocumentFormat(new ArrayList<>());

        //Then
        assertThat(errors).hasSize(0);
    }

    @Test
    void shouldReturnErrorsWhenCaseworkerCICDocumentIsNull() {
        //Given
        ListValue<CaseworkerCICDocumentUpload> document = getCaseworkerCICDocumentUpload(INVALID_FILE);
        List<ListValue<CaseworkerCICDocumentUpload>> documentList = new ArrayList<>();
        documentList.add(document);
        documentList.add(null);

        //When
        List<String> errors = DocumentUtil.validateCaseworkerCICDocumentFormat(documentList);

        //Then
        assertThat(errors).hasSize(1);
        assertThat(errors).contains(DOCUMENT_VALIDATION_MESSAGE);
    }

    @Test
    void shouldNotReturnErrorsForValidCaseworkerCICDocument() {
        //Given
        List<ListValue<CaseworkerCICDocumentUpload>> documentList = getCaseworkerCICDocumentUploadList(PDF_FILE);

        //When
        List<String> errors = DocumentUtil.validateCaseworkerCICDocumentFormat(documentList);

        //Then
        assertThat(errors).hasSize(0);
    }

    @Test
    void shouldNotReturnErrorsForValidCaseworkerCICDocuments() {
        //Given
        ListValue<CaseworkerCICDocumentUpload> document1 = getCaseworkerCICDocumentUpload(PDF_FILE);
        ListValue<CaseworkerCICDocumentUpload> document2 = getCaseworkerCICDocumentUpload(DOCX_FILE);
        List<ListValue<CaseworkerCICDocumentUpload>> documentList = new ArrayList<>();
        documentList.add(document1);
        documentList.add(document2);

        //When
        List<String> errors = DocumentUtil.validateCaseworkerCICDocumentFormat(documentList);

        //Then
        assertThat(errors).hasSize(0);
    }

    @Test
    void shouldReturnErrorsForInvalidCaseworkerCICDocument() {
        //Given
        List<ListValue<CaseworkerCICDocumentUpload>> documentList = getCaseworkerCICDocumentUploadList(INVALID_FILE);

        //When
        List<String> errors = DocumentUtil.validateCaseworkerCICDocumentFormat(documentList);

        //Then
        assertThat(errors).hasSize(1);
        assertThat(errors).contains(DOCUMENT_VALIDATION_MESSAGE);
    }

    @Test
    void shouldReturnErrorsForInvalidCaseworkerCICDocuments() {
        //Given
        ListValue<CaseworkerCICDocumentUpload> document1 = getCaseworkerCICDocumentUpload(PDF_FILE);
        ListValue<CaseworkerCICDocumentUpload> document2 = getCaseworkerCICDocumentUpload(INVALID_FILE);
        List<ListValue<CaseworkerCICDocumentUpload>> documentList = new ArrayList<>();
        documentList.add(document1);
        documentList.add(document2);

        //When
        List<String> errors = DocumentUtil.validateCaseworkerCICDocumentFormat(documentList);

        //Then
        assertThat(errors).hasSize(1);
        assertThat(errors).contains(DOCUMENT_VALIDATION_MESSAGE);
    }

    @Test
    void shouldNotReturnErrorsWhenDecisionDocumentIsNull() {
        //When
        List<String> errors = DocumentUtil.validateDecisionDocumentFormat(null);

        //Then
        assertThat(errors).hasSize(0);
    }

    @Test
    void shouldNotReturnErrorsWhenDecisionDocumentIsValid() {
        //Given
        final CICDocument document = createCICDocument(PDF_FILE);

        //When
        List<String> errors = DocumentUtil.validateDecisionDocumentFormat(document);

        //Then
        assertThat(errors).hasSize(0);
    }

    @Test
    void shouldReturnErrorsWhenDecisionDocumentIsInvalid() {
        //Given
        final CICDocument document = createCICDocument(INVALID_FILE);

        //When
        List<String> errors = DocumentUtil.validateDecisionDocumentFormat(document);

        //Then
        assertThat(errors).hasSize(1);
        assertThat(errors).contains(DOCUMENT_VALIDATION_MESSAGE);
    }

    @Test
    void shouldSuccessfullyAddDocument() {
        //Given
        final CaseData caseData = caseData();
        DocumentManagement documentManagement = DocumentManagement.builder()
            .caseworkerCICDocument(getCaseworkerCICDocumentList(PDF_FILE))
            .build();
        caseData.setAllDocManagement(documentManagement);

        //When
        DocumentUtil.uploadDocument(caseData);

        //Then
        assertThat(caseData.getAllDocManagement().getCaseworkerCICDocument()).hasSize(1);
    }

    @Test
    void shouldNotReturnErrorsWhenUploadedDocumentIsNull() {
        //When
        List<String> errors = DocumentUtil.validateUploadedDocuments(null);

        //Then
        assertThat(errors).hasSize(0);
    }

    @Test
    void shouldNotReturnErrorsWhenUploadedDocumentIsEmpty() {
        //When
        List<String> errors = DocumentUtil.validateUploadedDocuments(new ArrayList<>());

        //Then
        assertThat(errors).hasSize(0);
    }

    @Test
    void shouldNotReturnErrorsForValidUploadedDocuments() {
        //Given
        ListValue<CaseworkerCICDocumentUpload> document1 = getCaseworkerCICDocumentUpload(PDF_FILE);
        ListValue<CaseworkerCICDocumentUpload> document2 = getCaseworkerCICDocumentUpload(DOCX_FILE);
        List<ListValue<CaseworkerCICDocumentUpload>> documentList = new ArrayList<>();
        documentList.add(document1);
        documentList.add(document2);

        //When
        List<String> errors = DocumentUtil.validateUploadedDocuments(documentList);

        //Then
        assertThat(errors).hasSize(0);
    }

    @Test
    void shouldReturnErrorsWhenUploadedDocumentIsMissingLink() {
        //Given
        List<ListValue<CaseworkerCICDocumentUpload>> documentList = createCaseworkerCICDocumentUploadList(false, null, true, true);

        //When
        List<String> errors = DocumentUtil.validateUploadedDocuments(documentList);

        //Then
        assertThat(errors).hasSize(1);
        assertThat(errors).contains("Please attach the document");
    }

    @Test
    void shouldReturnErrorsWhenUploadedDocumentIsMissingEmailContent() {
        //Given
        List<ListValue<CaseworkerCICDocumentUpload>> documentList = createCaseworkerCICDocumentUploadList(true, PDF_FILE, false, true);

        //When
        List<String> errors = DocumentUtil.validateUploadedDocuments(documentList);

        //Then
        assertThat(errors).hasSize(1);
        assertThat(errors).contains("Description is mandatory for each document");
    }

    @Test
    void shouldReturnErrorsWhenUploadedDocumentIsMissingCategory() {
        //Given
        List<ListValue<CaseworkerCICDocumentUpload>> documentList = createCaseworkerCICDocumentUploadList(true, PDF_FILE, true, false);

        //When
        List<String> errors = DocumentUtil.validateUploadedDocuments(documentList);

        //Then
        assertThat(errors).hasSize(1);
        assertThat(errors).contains("Category is mandatory for each document");
    }

    @Test
    void shouldReturnErrorsForInvalidUploadedDocuments() {
        //Given
        final CaseworkerCICDocumentUpload document1 = createCaseworkerCICDocumentUpload(true, PDF_FILE, false, true);
        final CaseworkerCICDocumentUpload document2 = createCaseworkerCICDocumentUpload(true, PDF_FILE, true, false);
        final CaseworkerCICDocumentUpload document3 = createCaseworkerCICDocumentUpload(false, null, true, true);

        ListValue<CaseworkerCICDocumentUpload> documentListValue1 = new ListValue<>();
        documentListValue1.setValue(document1);
        ListValue<CaseworkerCICDocumentUpload> documentListValue2 = new ListValue<>();
        documentListValue2.setValue(document2);
        ListValue<CaseworkerCICDocumentUpload> documentListValue3 = new ListValue<>();
        documentListValue3.setValue(document3);

        List<ListValue<CaseworkerCICDocumentUpload>> documentList = new ArrayList<>();
        documentList.add(documentListValue1);
        documentList.add(documentListValue2);
        documentList.add(documentListValue3);

        //When
        List<String> errors = DocumentUtil.validateUploadedDocuments(documentList);

        //Then
        assertThat(errors).hasSize(3);
        assertThat(errors).contains("Description is mandatory for each document");
        assertThat(errors).contains("Category is mandatory for each document");
        assertThat(errors).contains("Please attach the document");
    }

    @Test
    void shouldAddDatesToUploadedDocuments() {
        final CaseworkerCICDocumentUpload documentUpload = createCaseworkerCICDocumentUpload(true, PDF_FILE, true, true);
        ListValue<CaseworkerCICDocumentUpload> documentUploadListValue = new ListValue<>();
        documentUploadListValue.setValue(documentUpload);
        List<ListValue<CaseworkerCICDocumentUpload>> documentUploadList = new ArrayList<>();
        documentUploadList.add(documentUploadListValue);

        List<ListValue<CaseworkerCICDocument>> outputList = DocumentUtil.convertToCaseworkerCICDocumentUpload(documentUploadList, true);

        assertThat(outputList).hasSize(1);
        assertThat(outputList.get(0).getValue().getDocumentCategory()).isEqualTo(documentUpload.getDocumentCategory());
        assertThat(outputList.get(0).getValue().getDocumentEmailContent()).isEqualTo(documentUpload.getDocumentEmailContent());
        assertThat(outputList.get(0).getValue().getDocumentLink()).isEqualTo(documentUpload.getDocumentLink());
        assertThat(outputList.get(0).getValue().getDate()).isNotNull();
    }

    @Test
    void shouldConvertDocumentsWithoutAddingDates() {
        final CaseworkerCICDocumentUpload documentUpload = createCaseworkerCICDocumentUpload(true, PDF_FILE, true, true);
        final ListValue<CaseworkerCICDocumentUpload> documentUploadListValue = new ListValue<>();
        documentUploadListValue.setValue(documentUpload);
        final List<ListValue<CaseworkerCICDocumentUpload>> documentUploadList = new ArrayList<>();
        documentUploadList.add(documentUploadListValue);

        List<ListValue<CaseworkerCICDocument>> outputList = DocumentUtil.convertToCaseworkerCICDocumentUpload(documentUploadList, false);

        assertThat(outputList).hasSize(1);
        assertThat(outputList.get(0).getValue().getDocumentCategory()).isEqualTo(documentUpload.getDocumentCategory());
        assertThat(outputList.get(0).getValue().getDocumentEmailContent()).isEqualTo(documentUpload.getDocumentEmailContent());
        assertThat(outputList.get(0).getValue().getDocumentLink()).isEqualTo(documentUpload.getDocumentLink());
        assertThat(outputList.get(0).getValue().getDate()).isNull();
    }

    @Test
    void shouldHandleConvertToCaseworkerCICDocumentUploadWithEmptyList() {
        final List<ListValue<CaseworkerCICDocumentUpload>> documentUploadList = new ArrayList<>();
        List<ListValue<CaseworkerCICDocument>> outputList = DocumentUtil.convertToCaseworkerCICDocumentUpload(documentUploadList, false);
        assertThat(outputList).hasSize(0);
    }

    @Test
    void shouldHandleConvertToCaseworkerCICDocumentUploadWithNullList() {
        List<ListValue<CaseworkerCICDocument>> outputList = DocumentUtil.convertToCaseworkerCICDocumentUpload(null, false);
        assertThat(outputList).hasSize(0);
    }

    @Test
    void shouldRemoveDatesFromUploadedDocuments() {
        final CaseworkerCICDocument document = createCaseworkerCICDocument(true, PDF_FILE, true, true);
        ListValue<CaseworkerCICDocument> documentListValue = new ListValue<>();
        documentListValue.setValue(document);
        List<ListValue<CaseworkerCICDocument>> documentList = new ArrayList<>();
        documentList.add(documentListValue);

        List<ListValue<CaseworkerCICDocumentUpload>> outputList = DocumentUtil.convertToCaseworkerCICDocument(documentList);

        assertThat(outputList).hasSize(1);
        assertThat(outputList.get(0).getValue().getDocumentCategory()).isEqualTo(document.getDocumentCategory());
        assertThat(outputList.get(0).getValue().getDocumentEmailContent()).isEqualTo(document.getDocumentEmailContent());
        assertThat(outputList.get(0).getValue().getDocumentLink()).isEqualTo(document.getDocumentLink());
    }

    @Test
    void shouldHandleConvertToCaseworkerCICDocumentWithEmptyList() {
        final List<ListValue<CaseworkerCICDocument>> documentUploadList = new ArrayList<>();
        List<ListValue<CaseworkerCICDocumentUpload>> outputList = DocumentUtil.convertToCaseworkerCICDocument(documentUploadList);
        assertThat(outputList).hasSize(0);
    }

    @Test
    void shouldHandleConvertToCaseworkerCICDocumentWithNullList() {
        List<ListValue<CaseworkerCICDocumentUpload>> outputList = DocumentUtil.convertToCaseworkerCICDocument(null);
        assertThat(outputList).hasSize(0);
    }

    @Test
    void shouldUpdateUploadedDocumentCategoryWithDate() {
        final CaseworkerCICDocumentUpload documentUpload = createCaseworkerCICDocumentUpload(true, PDF_FILE, true, true);
        final ListValue<CaseworkerCICDocumentUpload> documentUploadListValue = new ListValue<>();
        documentUploadListValue.setValue(documentUpload);
        final List<ListValue<CaseworkerCICDocumentUpload>> documentUploadList = new ArrayList<>();
        documentUploadList.add(documentUploadListValue);

        List<ListValue<CaseworkerCICDocument>> outputList = DocumentUtil.updateUploadedDocumentCategory(documentUploadList, true);

        assertThat(outputList).hasSize(1);
        assertThat(outputList.get(0).getValue().getDocumentCategory()).isEqualTo(documentUpload.getDocumentCategory());
        assertThat(outputList.get(0).getValue().getDocumentEmailContent()).isEqualTo(documentUpload.getDocumentEmailContent());
        assertThat(outputList.get(0).getValue().getDocumentLink()).isEqualTo(documentUpload.getDocumentLink());
        assertThat(outputList.get(0).getValue().getDate()).isNotNull();
    }

    @Test
    void shouldUpdateUploadedDocumentCategoryWithoutDate() {
        final CaseworkerCICDocumentUpload documentUpload = createCaseworkerCICDocumentUpload(true, PDF_FILE, true, true);
        final ListValue<CaseworkerCICDocumentUpload> documentUploadListValue = new ListValue<>();
        documentUploadListValue.setValue(documentUpload);
        final List<ListValue<CaseworkerCICDocumentUpload>> documentUploadList = new ArrayList<>();
        documentUploadList.add(documentUploadListValue);

        List<ListValue<CaseworkerCICDocument>> outputList = DocumentUtil.updateUploadedDocumentCategory(documentUploadList, false);

        assertThat(outputList).hasSize(1);
        assertThat(outputList.get(0).getValue().getDocumentCategory()).isEqualTo(documentUpload.getDocumentCategory());
        assertThat(outputList.get(0).getValue().getDocumentEmailContent()).isEqualTo(documentUpload.getDocumentEmailContent());
        assertThat(outputList.get(0).getValue().getDocumentLink()).isEqualTo(documentUpload.getDocumentLink());
        assertThat(outputList.get(0).getValue().getDate()).isNull();
    }

    private DocumentInfo documentInfo() {
        return new DocumentInfo(
            DOC_URL,
            PDF_FILENAME,
            DOC_BINARY_URL,
            CATEGORY_ID_VAL
        );
    }

    private CICDocument createCICDocument(String fileName) {
        return CICDocument.builder()
            .documentLink(Document.builder().filename(fileName).build())
            .documentEmailContent(EMAIL_CONTENT)
            .build();
    }

    private CaseworkerCICDocument createCaseworkerCICDocument(Boolean includeLink, String fileName,
                                                              Boolean includeContent, Boolean includeCategory) {
        CaseworkerCICDocument document = new CaseworkerCICDocument();

        if (includeLink && fileName != null) {
            document.setDocumentLink(Document.builder().filename(fileName).build());
        }

        if (includeContent) {
            document.setDocumentEmailContent(EMAIL_CONTENT);
        }

        if (includeCategory) {
            document.setDocumentCategory(DocumentType.LINKED_DOCS);
        }

        return document;
    }

    private List<ListValue<CaseworkerCICDocument>> createCaseworkerCICDocumentList(Boolean includeLink, String fileName,
                                                              Boolean includeContent, Boolean includeCategory) {
        CaseworkerCICDocument document = createCaseworkerCICDocument(includeLink, fileName, includeContent, includeCategory);

        ListValue<CaseworkerCICDocument> documentListValue = new ListValue<>();
        documentListValue.setValue(document);

        List<ListValue<CaseworkerCICDocument>> documentList = new ArrayList<>();
        documentList.add(documentListValue);

        return documentList;
    }

    private CaseworkerCICDocumentUpload createCaseworkerCICDocumentUpload(Boolean includeLink, String fileName,
                                                                          Boolean includeContent, Boolean includeCategory) {
        CaseworkerCICDocumentUpload document = new CaseworkerCICDocumentUpload();

        if (includeLink && fileName != null) {
            document.setDocumentLink(Document.builder().filename(fileName).build());
        }

        if (includeContent) {
            document.setDocumentEmailContent(EMAIL_CONTENT);
        }

        if (includeCategory) {
            document.setDocumentCategory(DocumentType.LINKED_DOCS);
        }

        return document;
    }

    private List<ListValue<CaseworkerCICDocumentUpload>> createCaseworkerCICDocumentUploadList(Boolean includeLink, String fileName,
                                                                                   Boolean includeContent, Boolean includeCategory) {
        CaseworkerCICDocumentUpload document = createCaseworkerCICDocumentUpload(includeLink, fileName, includeContent, includeCategory);

        ListValue<CaseworkerCICDocumentUpload> documentListValue = new ListValue<>();
        documentListValue.setValue(document);

        List<ListValue<CaseworkerCICDocumentUpload>> documentList = new ArrayList<>();
        documentList.add(documentListValue);

        return documentList;
    }

}
