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
    @Query("""
        update DocumentEntity d
        set d.caseDocumentTypeId = :documentTypeId
        where d.documentBinaryUrl = :documentBinaryUrl
        """)
    void updateDocumentTypeByDocumentBinaryUrl(
        @Param("documentBinaryUrl") String documentBinaryUrl,
        @Param("documentTypeId") Long documentTypeId
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
}
