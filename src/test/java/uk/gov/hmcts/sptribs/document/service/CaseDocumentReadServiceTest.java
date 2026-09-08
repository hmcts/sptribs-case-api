package uk.gov.hmcts.sptribs.document.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.repositories.DocumentsRepository;
import uk.gov.hmcts.sptribs.controllers.mapper.CaseDocumentProjectionMapper;
import uk.gov.hmcts.sptribs.controllers.mapper.CaseworkerCICDocumentMapper;
import uk.gov.hmcts.sptribs.document.model.BundleDocumentsView;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentType;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentView;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.document.model.SelectedCaseDocuments;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseDocumentReadServiceTest {

    private static final long CASE_REFERENCE = 1234567890123456L;

    @Mock
    private DocumentsRepository documentsRepository;

    @Mock
    private CaseDocumentTypesCache caseDocumentTypesCache;

    private CaseDocumentReadService service;

    @BeforeEach
    void setUp() {
        CaseworkerCICDocumentMapper caseworkerMapper = new CaseworkerCICDocumentMapper();
        CaseDocumentProjectionMapper projectionMapper = new CaseDocumentProjectionMapper(caseworkerMapper);
        service = new CaseDocumentReadService(
            documentsRepository,
            caseDocumentTypesCache,
            new CaseDocumentPolicy(),
            projectionMapper
        );
    }

    @Test
    void shouldBuildCaseViewWithStableDatabaseIdentityAndCompleteDocumentLink() {
        UUID documentId = UUID.randomUUID();
        DocumentEntity entity = document(41L, documentId, "application.pdf", 1L, DocumentType.APPLICATION_FORM);

        when(documentsRepository.findAllByCaseReferenceNumberOrderBySavedAtDesc(CASE_REFERENCE))
            .thenReturn(List.of(entity));
        when(caseDocumentTypesCache.getType(1L)).thenReturn(CaseDocumentType.APPLICATION);

        List<ListValue<CaseDocumentView>> result = service.getCaseViewDocuments(CASE_REFERENCE);

        assertThat(result).singleElement().satisfies(listValue -> {
            assertThat(listValue.getId()).isEqualTo("41");
            assertThat(listValue.getValue().getSourceType()).isEqualTo("Application");
            assertThat(listValue.getValue().getDocumentCategory()).isEqualTo(DocumentType.APPLICATION_FORM);
            assertThat(listValue.getValue().getDocumentLink())
                .extracting(Document::getUrl, Document::getBinaryUrl, Document::getFilename, Document::getCategoryId)
                .containsExactly(
                    documentUrl(documentId),
                    documentUrl(documentId) + "/binary",
                    "application.pdf",
                    DocumentType.APPLICATION_FORM.getCategory()
                );
        });
    }

    @Test
    void shouldExcludeCorrespondenceAndUnknownSourceTypesFromCaseView() {
        DocumentEntity correspondence = document(1L, UUID.randomUUID(), "correspondence.pdf", 8L,
            DocumentType.CORRESPONDENCE);
        DocumentEntity unknown = document(2L, UUID.randomUUID(), "unknown.pdf", 99L, DocumentType.LINKED_DOCS);

        when(documentsRepository.findAllByCaseReferenceNumberOrderBySavedAtDesc(CASE_REFERENCE))
            .thenReturn(List.of(correspondence, unknown));
        when(caseDocumentTypesCache.getType(8L)).thenReturn(CaseDocumentType.CORRESPONDENCE);
        when(caseDocumentTypesCache.getType(99L)).thenThrow(new IllegalArgumentException("Unknown type"));

        assertThat(service.getCaseViewDocuments(CASE_REFERENCE)).isEmpty();
    }

    @Test
    void shouldBuildBundleProjectionAndUseLegacyInitialListOnlyForClassification() {
        UUID initialId = UUID.randomUUID();
        UUID furtherId = UUID.randomUUID();
        DocumentEntity initial = document(1L, initialId, "initial.pdf", 1L, DocumentType.DSS_TRIBUNAL_FORM);
        DocumentEntity further = document(2L, furtherId, "further.docx", 2L, DocumentType.LINKED_DOCS);
        DocumentEntity recording = document(3L, UUID.randomUUID(), "recording.mp3", 7L, DocumentType.LINKED_DOCS);

        when(documentsRepository.findAllByCaseReferenceNumberOrderBySavedAtDesc(CASE_REFERENCE))
            .thenReturn(List.of(further, recording, initial));
        when(caseDocumentTypesCache.getType(1L)).thenReturn(CaseDocumentType.APPLICATION);
        when(caseDocumentTypesCache.getType(2L)).thenReturn(CaseDocumentType.DOCUMENT_MANAGEMENT);
        when(caseDocumentTypesCache.getType(7L)).thenReturn(CaseDocumentType.HEARING_RECORD);

        CaseworkerCICDocument legacyInitial = CaseworkerCICDocument.builder()
            .documentLink(Document.builder().binaryUrl(initial.getDocumentBinaryUrl()).build())
            .build();
        CaseData caseData = CaseData.builder()
            .initialCicaDocuments(List.of(ListValue.<CaseworkerCICDocument>builder().value(legacyInitial).build()))
            .build();

        BundleDocumentsView result = service.getBundleDocuments(CASE_REFERENCE, caseData);

        assertThat(result.getAllDocuments()).extracting(document -> document.getDocumentLink().getFilename())
            .containsExactly("further.docx", "initial.pdf");
        assertThat(result.getInitialDocuments()).extracting(document -> document.getDocumentLink().getFilename())
            .containsExactly("initial.pdf");
        assertThat(result.getFurtherDocuments()).extracting(document -> document.getDocumentLink().getFilename())
            .containsExactly("further.docx");
    }

    @Test
    void shouldBuildContactPartyOptionsWithStableCdamUuidCodes() {
        UUID pdfId = UUID.randomUUID();
        DocumentEntity pdf = document(1L, pdfId, "evidence.pdf", 2L, DocumentType.LINKED_DOCS);
        DocumentEntity video = document(2L, UUID.randomUUID(), "recording.mp4", 7L, DocumentType.LINKED_DOCS);

        when(documentsRepository.findAllByCaseReferenceNumberOrderBySavedAtDesc(CASE_REFERENCE))
            .thenReturn(List.of(pdf, video));
        when(caseDocumentTypesCache.getType(2L)).thenReturn(CaseDocumentType.DOCUMENT_MANAGEMENT);
        when(caseDocumentTypesCache.getType(7L)).thenReturn(CaseDocumentType.HEARING_RECORD);

        DynamicMultiSelectList result = service.getContactPartyOptions(CASE_REFERENCE, "http://case-api/");

        assertThat(result.getListItems()).singleElement().satisfies(option -> {
            assertThat(option.getCode()).isEqualTo(pdfId);
            assertThat(option.getLabel()).isEqualTo(
                "[evidence.pdf " + DocumentType.LINKED_DOCS.getLabel() + "]"
                    + "(http://case-api/documents/" + pdfId + "/binary)"
            );
        });
        assertThat(result.getValue()).isEmpty();
    }

    @Test
    void shouldResolveSelectedDocumentsByCodeWithinTheCaseAndPreserveSelectionOrder() {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        UUID otherCaseId = UUID.randomUUID();
        DocumentEntity first = document(1L, firstId, "first.pdf", 2L, DocumentType.LINKED_DOCS);
        DocumentEntity second = document(2L, secondId, "second.pdf", 2L, DocumentType.TRIBUNAL_DIRECTION);

        when(documentsRepository.findAllByCaseReferenceNumberOrderBySavedAtDesc(CASE_REFERENCE))
            .thenReturn(List.of(first, second));
        when(caseDocumentTypesCache.getType(2L)).thenReturn(CaseDocumentType.DOCUMENT_MANAGEMENT);

        DynamicMultiSelectList selection = DynamicMultiSelectList.builder()
            .value(List.of(option(secondId), option(otherCaseId), option(firstId)))
            .build();

        SelectedCaseDocuments result = service.getSelectedContactPartyDocuments(CASE_REFERENCE, selection, 10);

        assertThat(result.getDocumentEntityIds()).containsExactly(2L, 1L);
        assertThat(result.getDocuments()).extracting(document -> document.getDocumentLink().getFilename())
            .containsExactly("second.pdf", "first.pdf");
    }

    private DynamicListElement option(UUID id) {
        return DynamicListElement.builder().code(id).label("label is not parsed").build();
    }

    private DocumentEntity document(long id,
                                    UUID documentId,
                                    String filename,
                                    long caseDocumentTypeId,
                                    DocumentType category) {
        return DocumentEntity.builder()
            .id(id)
            .caseReferenceNumber(CASE_REFERENCE)
            .savedAt(OffsetDateTime.parse("2026-09-08T10:15:30Z").plusMinutes(id))
            .documentUrl(documentUrl(documentId))
            .documentBinaryUrl(documentUrl(documentId) + "/binary")
            .documentFilename(filename)
            .documentTypeName(category.name())
            .caseDocumentTypeId(caseDocumentTypeId)
            .build();
    }

    private String documentUrl(UUID documentId) {
        return "http://document-management/documents/" + documentId;
    }
}
