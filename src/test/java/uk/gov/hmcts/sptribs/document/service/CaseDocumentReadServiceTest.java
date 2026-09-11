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
import uk.gov.hmcts.sptribs.common.repositories.DocumentsRepository;
import uk.gov.hmcts.sptribs.controllers.mapper.CaseworkerCICDocumentMapper;
import uk.gov.hmcts.sptribs.document.exception.DocumentSelectionException;
import uk.gov.hmcts.sptribs.document.model.BundleDocumentsView;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentType;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentView;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.document.model.SelectedCaseDocuments;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
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
        lenient().when(caseDocumentTypesCache.getId(any(CaseDocumentType.class)))
            .thenAnswer(invocation -> (long) invocation.<CaseDocumentType>getArgument(0).ordinal() + 1);
        service = new CaseDocumentReadService(
            documentsRepository,
            caseDocumentTypesCache,
            new CaseworkerCICDocumentMapper()
        );
    }

    @Test
    void shouldBuildCaseViewFromEligibleDocumentTypesQueriedFromDatabase() {
        UUID documentId = UUID.randomUUID();
        DocumentEntity entity = document(41L, documentId, "application.pdf", 1L, DocumentType.APPLICATION_FORM);

        when(documentsRepository.findDocumentsByReferenceAndCaseDocumentTypeIds(eq(CASE_REFERENCE), anyList()))
            .thenReturn(List.of(entity));

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

        verify(documentsRepository).findDocumentsByReferenceAndCaseDocumentTypeIds(
            eq(CASE_REFERENCE),
            org.mockito.ArgumentMatchers.argThat(typeIds -> {
                assertThat(typeIds)
                    .containsExactlyInAnyOrder(1L, 2L, 3L, 4L, 5L, 6L, 7L, 10L)
                    .doesNotContain(8L, 9L);
                return true;
            })
        );
    }

    @Test
    void shouldBuildBundleProjectionAndUseInitialBinaryUrlsOnlyForClassification() {
        UUID initialId = UUID.randomUUID();
        UUID furtherId = UUID.randomUUID();
        DocumentEntity initial = document(1L, initialId, "initial.pdf", 1L, DocumentType.DSS_TRIBUNAL_FORM);
        DocumentEntity further = document(2L, furtherId, "further.docx", 2L, DocumentType.LINKED_DOCS);
        DocumentEntity recording = document(3L, UUID.randomUUID(), "recording.mp3", 7L, DocumentType.LINKED_DOCS);

        when(documentsRepository.findDocumentsByReferenceAndCaseDocumentTypeIds(eq(CASE_REFERENCE), anyList()))
            .thenReturn(List.of(further, recording, initial));

        BundleDocumentsView result = service.getBundleDocuments(
            CASE_REFERENCE,
            Set.of(initial.getDocumentBinaryUrl())
        );

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

        when(documentsRepository.findDocumentsByReferenceAndCaseDocumentTypeIds(eq(CASE_REFERENCE), anyList()))
            .thenReturn(List.of(pdf, video));

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
    void shouldQueryOnlySelectedDocumentsWithinTheCaseAndPreserveSelectionOrder() {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        DocumentEntity first = document(1L, firstId, "first.pdf", 2L, DocumentType.LINKED_DOCS);
        DocumentEntity second = document(2L, secondId, "second.pdf", 2L, DocumentType.TRIBUNAL_DIRECTION);

        when(documentsRepository.findByCaseReferenceAndDocumentIdUuid(CASE_REFERENCE, secondId.toString()))
            .thenReturn(Optional.of(second));
        when(documentsRepository.findByCaseReferenceAndDocumentIdUuid(CASE_REFERENCE, firstId.toString()))
            .thenReturn(Optional.of(first));

        DynamicMultiSelectList selection = DynamicMultiSelectList.builder()
            .value(List.of(option(secondId), option(firstId)))
            .build();

        SelectedCaseDocuments result = service.getSelectedContactPartyDocuments(CASE_REFERENCE, selection, 10);

        assertThat(result.getDocumentEntityIds()).containsExactly(2L, 1L);
        assertThat(result.getDocuments()).extracting(document -> document.getDocumentLink().getFilename())
            .containsExactly("second.pdf", "first.pdf");
    }

    @Test
    void shouldRejectSelectionWhenDocumentIsNotAvailableForTheCase() {
        UUID missingId = UUID.randomUUID();
        DynamicMultiSelectList selection = DynamicMultiSelectList.builder()
            .value(List.of(option(missingId)))
            .build();

        when(documentsRepository.findByCaseReferenceAndDocumentIdUuid(CASE_REFERENCE, missingId.toString()))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSelectedContactPartyDocuments(CASE_REFERENCE, selection, 10))
            .isInstanceOf(DocumentSelectionException.class)
            .hasMessageContaining(missingId.toString())
            .hasMessageContaining(String.valueOf(CASE_REFERENCE));
    }

    @Test
    void shouldRejectSelectionsOverTheAttachmentLimitWithoutQueryingTheDatabase() {
        DynamicMultiSelectList selection = DynamicMultiSelectList.builder()
            .value(List.of(option(UUID.randomUUID()), option(UUID.randomUUID())))
            .build();

        assertThatThrownBy(() -> service.getSelectedContactPartyDocuments(CASE_REFERENCE, selection, 1))
            .isInstanceOf(DocumentSelectionException.class)
            .hasMessageContaining("permitted limit is 1");
    }

    @Test
    void shouldNotQueryDocumentsForAnEmptySelection() {
        SelectedCaseDocuments result = service.getSelectedContactPartyDocuments(
            CASE_REFERENCE,
            DynamicMultiSelectList.builder().value(List.of()).build(),
            10
        );

        assertThat(result.getDocumentEntityIds()).isEmpty();
        assertThat(result.getDocuments()).isEmpty();
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
