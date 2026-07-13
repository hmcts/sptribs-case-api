package uk.gov.hmcts.sptribs.document.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.sptribs.common.repositories.CorrespondenceDocumentRepository;
import uk.gov.hmcts.sptribs.common.repositories.exception.correspondencedocument.CorrespondenceDocumentSaveException;
import uk.gov.hmcts.sptribs.common.repositories.model.CorrespondenceDocumentEntity;
import uk.gov.hmcts.sptribs.common.repositories.model.CorrespondenceDocumentId;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class CorrespondenceDocumentService {

    private final CorrespondenceDocumentRepository correspondenceDocumentRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveCorrespondenceDocumentLink(String correspondenceId, List<Long> documentIds) {

        try {

            List<CorrespondenceDocumentEntity> entities = documentIds.stream()
                .map(documentId -> CorrespondenceDocumentEntity.builder()
                    .id(CorrespondenceDocumentId.builder()
                        .correspondenceId(UUID.fromString(correspondenceId))
                        .documentId(documentId)
                        .build())
                    .build())
                .toList();

            correspondenceDocumentRepository.saveAll(entities);

        } catch (DataAccessException e) {
            log.error(
                "Failed to save document links for correspondenceId {}.",
                correspondenceId,
                e
            );

            throw new CorrespondenceDocumentSaveException(
                String.format("Failed to save document links for correspondenceId %s.", correspondenceId), e);
        }

    }
}
