package uk.gov.hmcts.sptribs.controllers.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CaseworkerCICDocumentMapperTest {

    private final CaseworkerCICDocumentMapper mapper = new CaseworkerCICDocumentMapper();

    @Test
    void shouldMapDocumentEntityToCaseworkerCicDocument() {
        // Given
        OffsetDateTime savedAt = OffsetDateTime.parse("2026-06-05T10:15:30Z");

        DocumentEntity entity = DocumentEntity.builder()
            .documentFilename("test-document.pdf")
            .documentUrl("http://test-url")
            .documentTypeName(DocumentType.TRIBUNAL_DIRECTION.name())
            .savedAt(savedAt)
            .build();

        // When
        CaseworkerCICDocument result = mapper.map(entity);

        // Then
        assertThat(result).isNotNull();

        Document document = result.getDocumentLink();
        assertThat(document.getUrl()).isEqualTo("http://test-url");
        assertThat(document.getFilename()).isEqualTo("test-document.pdf");

        assertThat(result.getDocumentCategory())
            .isEqualTo(DocumentType.TRIBUNAL_DIRECTION);

        assertThat(result.getDate())
            .isEqualTo(savedAt.toLocalDate());
    }

    @Test
    void shouldMapDocumentEntityToCaseworkerCicDocumentWithNullDocTypeName() {
        // Given
        OffsetDateTime savedAt = OffsetDateTime.parse("2026-06-05T10:15:30Z");

        DocumentEntity entity = DocumentEntity.builder()
            .documentFilename("test-document.pdf")
            .documentUrl("http://test-url")
            .savedAt(savedAt)
            .build();

        // When
        CaseworkerCICDocument result = mapper.map(entity);

        // Then
        assertThat(result).isNotNull();

        Document document = result.getDocumentLink();
        assertThat(document.getUrl()).isEqualTo("http://test-url");
        assertThat(document.getFilename()).isEqualTo("test-document.pdf");

        assertThat(result.getDocumentCategory())
            .isEqualTo(null);

        assertThat(result.getDate())
            .isEqualTo(savedAt.toLocalDate());
    }

    @Test
    void shouldMapListOfDocumentEntities() {
        // Given
        DocumentEntity entity1 = DocumentEntity.builder()
            .documentFilename("document-1.pdf")
            .documentUrl("url-1")
            .documentBinaryUrl("test-url/binary")
            .documentTypeName(DocumentType.TRIBUNAL_DIRECTION.name())
            .savedAt(OffsetDateTime.now())
            .build();

        DocumentEntity entity2 = DocumentEntity.builder()
            .documentFilename("document-2.pdf")
            .documentUrl("url-2")
            .documentBinaryUrl("test-url-2/binary")
            .documentTypeName(DocumentType.APPLICATION_FORM.name())
            .savedAt(OffsetDateTime.now())
            .build();

        // When
        List<CaseworkerCICDocument> result = mapper.map(List.of(entity1, entity2));

        // Then
        assertThat(result).hasSize(2);

        assertThat(result.get(0).getDocumentLink().getUrl())
            .isEqualTo("url-1");

        assertThat(result.get(1).getDocumentLink().getUrl())
            .isEqualTo("url-2");
    }

    @Test
    void shouldMapEntityToSingleItemList() {
        // Given
        DocumentEntity entity = DocumentEntity.builder()
            .documentFilename("test-document.pdf")
            .documentUrl("test-url")
            .documentBinaryUrl("test-url/binary")
            .documentTypeName(DocumentType.TRIBUNAL_DIRECTION.name())
            .savedAt(OffsetDateTime.now())
            .build();

        // When
        List<CaseworkerCICDocument> result = mapper.mapEntityToList(entity);

        // Then
        assertThat(result).hasSize(1);

        assertThat(result.getFirst().getDocumentLink().getUrl())
            .isEqualTo("test-url");

        assertThat(result.getFirst().getDocumentCategory())
            .isEqualTo(DocumentType.TRIBUNAL_DIRECTION);
    }

    @Test
    void shouldReturnEmptyWhenNoDocsInBundle() {

        //when
        List<CaseworkerCICDocument> result = mapper.mapEntityToList(null);

        //then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenNoDocsInModel() {

        //when
        List<CaseworkerCICDocument> result = mapper.map(List.of());

        //then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenNullDocsInModel() {

        //when
        List<CaseworkerCICDocument> result = mapper.map((List<DocumentEntity>) null);

        //then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldMapDocumentEntityWithDownloadedStatusTrue() {
        // Given
        DocumentEntity entity = DocumentEntity.builder()
            .id(101L)
            .documentFilename("test.pdf")
            .documentUrl("http://url")
            .savedAt(OffsetDateTime.now())
            .build();

        Set<Long> downloadedDocIds = Set.of(101L, 102L);

        // When
        CaseworkerCICDocument result = mapper.map(entity, downloadedDocIds);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isDownloaded()).isTrue();
    }

    @Test
    void shouldMapDocumentEntityWithDownloadedStatusFalse() {
        // Given
        DocumentEntity entity = DocumentEntity.builder()
            .id(103L)
            .documentFilename("test.pdf")
            .documentUrl("http://url")
            .savedAt(OffsetDateTime.now())
            .build();

        Set<Long> downloadedDocIds = Set.of(101L, 102L);

        // When
        CaseworkerCICDocument result = mapper.map(entity, downloadedDocIds);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isDownloaded()).isFalse();
    }

    @Test
    void shouldMapListOfEntitiesWithDownloadedStatus() {
        // Given
        DocumentEntity entity1 = DocumentEntity.builder()
            .id(101L)
            .documentFilename("test1.pdf")
            .documentUrl("http://url1")
            .savedAt(OffsetDateTime.now())
            .build();

        DocumentEntity entity2 = DocumentEntity.builder()
            .id(102L)
            .documentFilename("test2.pdf")
            .documentUrl("http://url2")
            .savedAt(OffsetDateTime.now())
            .build();

        Set<Long> downloadedDocIds = Set.of(101L);

        // When
        List<CaseworkerCICDocument> result = mapper.map(List.of(entity1, entity2), downloadedDocIds);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).isDownloaded()).isTrue();
        assertThat(result.get(1).isDownloaded()).isFalse();
    }

    @Test
    void shouldMapEntityToSingleItemListWithDownloadedStatus() {
        // Given
        DocumentEntity entity = DocumentEntity.builder()
            .id(102L)
            .documentFilename("test2.pdf")
            .documentUrl("http://url2")
            .savedAt(OffsetDateTime.now())
            .build();

        Set<Long> downloadedDocIds = Set.of(102L);

        // When
        List<CaseworkerCICDocument> result = mapper.mapEntityToList(entity, downloadedDocIds);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().isDownloaded()).isTrue();
    }

    @Test
    void shouldReturnEmptyWhenNullDocsInModelWithDownloadedStatus() {
        // When
        List<CaseworkerCICDocument> resultList = mapper.map((List<DocumentEntity>) null, Set.of(1L));
        List<CaseworkerCICDocument> resultEntity = mapper.mapEntityToList(null, Set.of(1L));

        // Then
        assertThat(resultList).isEmpty();
        assertThat(resultEntity).isEmpty();
    }

    @Test
    void shouldMapEmbeddedDownloadStatusIndependentlyPerParty() {
        // Given
        DocumentEntity entity1 = DocumentEntity.builder()
            .id(1L)
            .documentFilename("test1.pdf")
            .documentUrl("http://url1")
            .savedAt(OffsetDateTime.now())
            .build();

        DocumentEntity entity2 = DocumentEntity.builder()
            .id(2L)
            .documentFilename("test2.pdf")
            .documentUrl("http://url2")
            .savedAt(OffsetDateTime.now())
            .build();

        DocumentEntity entity3 = DocumentEntity.builder()
            .id(3L)
            .documentFilename("test3.pdf")
            .documentUrl("http://url3")
            .savedAt(OffsetDateTime.now())
            .build();

        List<DocumentEntity> documentsList = List.of(entity1, entity2, entity3);

        // Subject download history: Doc 1 & Doc 2
        Set<Long> subjectDownloadedDocIds = Set.of(1L, 2L);
        List<CaseworkerCICDocument> subjectMappedDocs = mapper.map(documentsList, subjectDownloadedDocIds);
        assertThat(subjectMappedDocs.get(0).isDownloaded()).isTrue();
        assertThat(subjectMappedDocs.get(1).isDownloaded()).isTrue();
        assertThat(subjectMappedDocs.get(2).isDownloaded()).isFalse();

        // Applicant download history: Doc 3 only
        Set<Long> applicantDownloadedDocIds = Set.of(3L);
        List<CaseworkerCICDocument> applicantMappedDocs = mapper.map(documentsList, applicantDownloadedDocIds);
        assertThat(applicantMappedDocs.get(0).isDownloaded()).isFalse();
        assertThat(applicantMappedDocs.get(1).isDownloaded()).isFalse();
        assertThat(applicantMappedDocs.get(2).isDownloaded()).isTrue();
    }
}


