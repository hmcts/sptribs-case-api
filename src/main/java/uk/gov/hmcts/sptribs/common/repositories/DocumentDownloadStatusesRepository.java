package uk.gov.hmcts.sptribs.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.sptribs.document.model.DocumentDownloadStatusEntity;
import uk.gov.hmcts.sptribs.notification.model.Party;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentDownloadStatusesRepository extends JpaRepository<DocumentDownloadStatusEntity, Long> {
    List<DocumentDownloadStatusEntity> findAllByCaseReferenceNumber(Long caseReferenceNumber);

    List<DocumentDownloadStatusEntity> findAllByDocumentId(long documentId);

    Optional<DocumentDownloadStatusEntity> findByDocumentIdAndParty(long documentId, Party party);
}
