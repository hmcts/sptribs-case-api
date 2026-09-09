package uk.gov.hmcts.sptribs.document.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import uk.gov.hmcts.sptribs.common.repositories.CorrespondenceDocumentRepository;
import uk.gov.hmcts.sptribs.common.repositories.exception.correspondencedocument.CorrespondenceDocumentSaveException;
import uk.gov.hmcts.sptribs.common.repositories.model.CorrespondenceDocumentEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CorrespondenceDocumentServiceTest {

    @InjectMocks
    private CorrespondenceDocumentService correspondenceDocumentService;
    @Mock
    private CorrespondenceDocumentRepository correspondenceDocumentRepository;
    @Captor
    private ArgumentCaptor<List<CorrespondenceDocumentEntity>> correspondenceDocumentEntitiesCaptor;

    @Test
    void shouldSaveCorrespondenceDocumentLink() {
        //given
        String correspondenceId = UUID.randomUUID().toString();
        List<Long> documentIds = List.of(1L, 2L, 3L);

        //when
        correspondenceDocumentService.saveCorrespondenceDocumentLink(correspondenceId, documentIds);

        //then
        verify(correspondenceDocumentRepository).saveAll(correspondenceDocumentEntitiesCaptor.capture());

        List<CorrespondenceDocumentEntity> savedEntities =
            correspondenceDocumentEntitiesCaptor.getValue();

        assertThat(savedEntities).hasSize(3);

        assertThat(savedEntities)
            .extracting(e -> e.getId().getDocumentId())
            .containsExactly(1L, 2L, 3L);

        assertThat(savedEntities)
            .extracting(e -> e.getId().getCorrespondenceId())
            .containsOnly(UUID.fromString(correspondenceId));

    }

    @Test
    void shouldThrowCorrespondenceDocumentSaveExceptionAfterDBError() {
        //given
        UUID correspondenceId = UUID.randomUUID();
        List<Long> documentIds = List.of(1L, 2L, 3L);

        DataAccessException dataAccessException =
            new DataIntegrityViolationException("Database error");

        doThrow(dataAccessException)
            .when(correspondenceDocumentRepository)
            .saveAll(anyList());

        //when / then
        assertThatThrownBy(() ->
            correspondenceDocumentService.saveCorrespondenceDocumentLink(
                correspondenceId.toString(),
                documentIds
            )
        )
            .isInstanceOf(CorrespondenceDocumentSaveException.class)
            .hasMessage(
                "Failed to save document links for correspondenceId %s."
                    .formatted(correspondenceId)
            )
            .hasCause(dataAccessException);

        verify(correspondenceDocumentRepository).saveAll(anyList());
    }

}
