package uk.gov.hmcts.sptribs.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;

import java.util.List;

@Repository
public interface DocumentsRepository extends JpaRepository<DocumentEntity, Integer> {
    @Modifying
    @Query("update DocumentEntity d set d.sentToApplicantViaContactParties = true where d.documentBinaryUrl = :documentBinaryUrl")
    void setSentToApplicantViaContactPartiesToTrueByDocumentBinaryUrl(@Param("documentBinaryUrl") String documentBinaryUrl);

    List<DocumentEntity> findByCaseReferenceNumberOrderBySavedAtDesc(
        Long caseReferenceNumber);
}
