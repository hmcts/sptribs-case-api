package uk.gov.hmcts.sptribs.controllers.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

import java.time.OffsetDateTime;
import java.util.List;

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
}
