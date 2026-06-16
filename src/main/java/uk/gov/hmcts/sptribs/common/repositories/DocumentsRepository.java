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
    @Query("""
        update DocumentEntity d
        set d.sentToApplicantViaContactParties = true
        where d.documentBinaryUrl in :documentUrls
        """)
    int setSentToApplicantViaContactPartiesToTrueByDocumentBinaryUrl(
        @Param("documentUrls") List<String> documentUrls
    );

    @Modifying
    @Query("update DocumentEntity d set d.isDraft = false where d.documentBinaryUrl = :documentBinaryUrl")
    void setIsDraftToFalseByDocumentBinaryUrl(@Param("documentBinaryUrl") String documentBinaryUrl);

    @Query("""
            SELECT d
            FROM DocumentEntity d
            WHERE d.caseReferenceNumber = :caseReferenceNumber
              AND d.isDraft = false
            ORDER BY d.savedAt DESC
        """)
    List<DocumentEntity> findAllNonDraftDocumentsByCaseReference(
        @Param("caseReferenceNumber") Long caseReferenceNumber);

    @Query("""
    SELECT d
    FROM DocumentEntity d
    WHERE d.caseReferenceNumber = :caseReferenceNumber
    AND d.documentBinaryUrl = :documentBinaryUrl
        """)
    DocumentEntity findDocumentByCaseReferenceAndBinaryUrl(
        @Param("caseReferenceNumber") Long caseReferenceNumber,
        @Param("documentBinaryUrl") String documentBinaryUrl);
}
