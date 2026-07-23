package uk.gov.hmcts.sptribs.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;

import java.util.List;
import java.util.Optional;

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
    @Query("""
        update DocumentEntity d
        set d.documentTypeName = :documentTypeName,
            d.updatedAt = current_instant()
        where d.documentBinaryUrl = :documentBinaryUrl
        """)
    int setDocumentTypeNameByDocumentBinaryUrl(
        @Param("documentBinaryUrl") String documentBinaryUrl,
        @Param("documentTypeName") String documentTypeName
    );

    @Modifying
    @Query("""
        update DocumentEntity d
        set d.caseDocumentTypeId = :caseDocumentTypeId,
            d.updatedAt = current_instant()
        where d.documentBinaryUrl = :documentBinaryUrl
        """)
    void updateCaseDocumentTypeIdByDocumentBinaryUrl(
        @Param("documentBinaryUrl") String documentBinaryUrl,
        @Param("caseDocumentTypeId") Long caseDocumentTypeId
    );

    @Query("""
            SELECT d
            FROM DocumentEntity d
            WHERE d.caseReferenceNumber = :caseReferenceNumber
            ORDER BY d.savedAt DESC
        """)
    List<DocumentEntity> findAllDocumentsByCaseReference(
        @Param("caseReferenceNumber") Long caseReferenceNumber);

    @Modifying
    @Query("""
            DELETE
            FROM DocumentEntity d
            WHERE d.documentBinaryUrl = :documentBinaryUrl
        """)
    void deleteEntryByBinaryURL(
        @Param("documentBinaryUrl") String documentBinaryUrl);

    @Query("""
            SELECT d
            FROM DocumentEntity d
            WHERE d.documentUrl LIKE %:documentId% OR d.documentBinaryUrl LIKE %:documentId%
        """)
    Optional<DocumentEntity> findByDocumentIdUuid(
        @Param("documentId") String documentId);
}
