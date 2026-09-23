package uk.gov.hmcts.sptribs.controllers.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.ContactPartyDocumentDetails;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CaseworkerCICDocumentMapperTest {

    private final CaseworkerCICDocumentMapper mapper =
        new CaseworkerCICDocumentMapper();

    @Test
    void shouldMapDocumentEntityToCaseworkerCicDocument() {
        // Given
        OffsetDateTime savedAt =
            OffsetDateTime.parse("2026-06-05T10:15:30Z");

        DocumentEntity entity = DocumentEntity.builder()
            .documentFilename("test-document.pdf")
            .documentUrl("http://test-url")
            .documentTypeName(DocumentType.TRIBUNAL_DIRECTION.name())
            .savedAt(savedAt)
            .build();

        // When
        CaseworkerCICDocument result = mapper.mapDocument(entity);

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
    void shouldUseUpdatedAtWhenPresent() {
        // Given
        OffsetDateTime savedAt =
            OffsetDateTime.parse("2026-06-05T10:15:30Z");
        OffsetDateTime updatedAt =
            OffsetDateTime.parse("2026-06-10T14:30:00Z");

        DocumentEntity entity = DocumentEntity.builder()
            .documentFilename("test-document.pdf")
            .documentUrl("http://test-url")
            .documentTypeName(DocumentType.TRIBUNAL_DIRECTION.name())
            .savedAt(savedAt)
            .updatedAt(updatedAt)
            .build();

        // When
        CaseworkerCICDocument result = mapper.mapDocument(entity);

        // Then
        assertThat(result.getDate())
            .isEqualTo(updatedAt.toLocalDate());
    }

    @Test
    void shouldMapDocumentEntityWithNullDocumentTypeName() {
        // Given
        OffsetDateTime savedAt =
            OffsetDateTime.parse("2026-06-05T10:15:30Z");

        DocumentEntity entity = DocumentEntity.builder()
            .documentFilename("test-document.pdf")
            .documentUrl("http://test-url")
            .savedAt(savedAt)
            .build();

        // When
        CaseworkerCICDocument result = mapper.mapDocument(entity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDocumentLink().getUrl())
            .isEqualTo("http://test-url");
        assertThat(result.getDocumentLink().getFilename())
            .isEqualTo("test-document.pdf");
        assertThat(result.getDocumentCategory()).isNull();
        assertThat(result.getDate())
            .isEqualTo(savedAt.toLocalDate());
    }

    @Test
    void shouldMapContactPartyDocumentUsingSentOnDate() {
        // Given
        OffsetDateTime savedAt =
            OffsetDateTime.parse("2026-06-05T10:15:30Z");
        OffsetDateTime sentOn =
            OffsetDateTime.parse("2026-06-12T16:45:00Z");

        DocumentEntity entity = DocumentEntity.builder()
            .documentFilename("contact-party-document.pdf")
            .documentUrl("http://contact-party-url")
            .documentTypeName(DocumentType.TRIBUNAL_DIRECTION.name())
            .savedAt(savedAt)
            .build();

        ContactPartyDocumentDetails details =
            new ContactPartyDocumentDetails(entity, sentOn);

        // When
        CaseworkerCICDocument result =
            mapper.mapContactPartyDocument(details);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDocumentLink().getUrl())
            .isEqualTo("http://contact-party-url");
        assertThat(result.getDocumentLink().getFilename())
            .isEqualTo("contact-party-document.pdf");
        assertThat(result.getDocumentCategory())
            .isEqualTo(DocumentType.TRIBUNAL_DIRECTION);
        assertThat(result.getDate())
            .isEqualTo(sentOn.toLocalDate());
    }

    @Test
    void shouldMapListOfDocumentEntities() {
        // Given
        OffsetDateTime savedAt =
            OffsetDateTime.parse("2026-06-05T10:15:30Z");

        DocumentEntity entity1 = DocumentEntity.builder()
            .documentFilename("document-1.pdf")
            .documentUrl("url-1")
            .documentBinaryUrl("test-url/binary")
            .documentTypeName(DocumentType.TRIBUNAL_DIRECTION.name())
            .savedAt(savedAt)
            .build();

        DocumentEntity entity2 = DocumentEntity.builder()
            .documentFilename("document-2.pdf")
            .documentUrl("url-2")
            .documentBinaryUrl("test-url-2/binary")
            .documentTypeName(DocumentType.APPLICATION_FORM.name())
            .savedAt(savedAt)
            .build();

        // When
        List<CaseworkerCICDocument> result =
            mapper.mapDocuments(List.of(entity1, entity2));

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDocumentLink().getUrl())
            .isEqualTo("url-1");
        assertThat(result.get(1).getDocumentLink().getUrl())
            .isEqualTo("url-2");
    }

    @Test
    void shouldMapListOfContactPartyDocuments() {
        // Given
        OffsetDateTime sentOn1 =
            OffsetDateTime.parse("2026-06-12T10:00:00Z");
        OffsetDateTime sentOn2 =
            OffsetDateTime.parse("2026-06-13T11:00:00Z");

        DocumentEntity entity1 = DocumentEntity.builder()
            .documentFilename("document-1.pdf")
            .documentUrl("url-1")
            .documentTypeName(DocumentType.TRIBUNAL_DIRECTION.name())
            .savedAt(OffsetDateTime.parse("2026-06-05T10:00:00Z"))
            .build();

        DocumentEntity entity2 = DocumentEntity.builder()
            .documentFilename("document-2.pdf")
            .documentUrl("url-2")
            .documentTypeName(DocumentType.APPLICATION_FORM.name())
            .savedAt(OffsetDateTime.parse("2026-06-06T10:00:00Z"))
            .build();

        List<ContactPartyDocumentDetails> details = List.of(
            new ContactPartyDocumentDetails(entity1, sentOn1),
            new ContactPartyDocumentDetails(entity2, sentOn2)
        );

        // When
        List<CaseworkerCICDocument> result =
            mapper.mapContactPartyDocuments(details);

        // Then
        assertThat(result).hasSize(2);

        assertThat(result.get(0).getDocumentLink().getUrl())
            .isEqualTo("url-1");
        assertThat(result.get(0).getDate())
            .isEqualTo(sentOn1.toLocalDate());

        assertThat(result.get(1).getDocumentLink().getUrl())
            .isEqualTo("url-2");
        assertThat(result.get(1).getDate())
            .isEqualTo(sentOn2.toLocalDate());
    }

    @Test
    void shouldMapDocumentEntityToSingleItemList() {
        // Given
        DocumentEntity entity = DocumentEntity.builder()
            .documentFilename("test-document.pdf")
            .documentUrl("test-url")
            .documentBinaryUrl("test-url/binary")
            .documentTypeName(DocumentType.TRIBUNAL_DIRECTION.name())
            .savedAt(OffsetDateTime.parse("2026-06-05T10:15:30Z"))
            .build();

        // When
        List<CaseworkerCICDocument> result =
            mapper.mapDocumentToList(entity);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getDocumentLink().getUrl())
            .isEqualTo("test-url");
        assertThat(result.getFirst().getDocumentCategory())
            .isEqualTo(DocumentType.TRIBUNAL_DIRECTION);
    }

    @Test
    void shouldReturnEmptyListWhenBundleDocumentIsNull() {
        // When
        List<CaseworkerCICDocument> result =
            mapper.mapDocumentToList(null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenDocumentsAreEmpty() {
        // When
        List<CaseworkerCICDocument> result =
            mapper.mapDocuments(List.of());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenDocumentsAreNull() {
        // When
        List<CaseworkerCICDocument> result =
            mapper.mapDocuments(null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenContactPartyDocumentsAreEmpty() {
        // When
        List<CaseworkerCICDocument> result =
            mapper.mapContactPartyDocuments(List.of());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenContactPartyDocumentsAreNull() {
        // When
        List<CaseworkerCICDocument> result =
            mapper.mapContactPartyDocuments(null);

        // Then
        assertThat(result).isEmpty();
    }
}
