package uk.gov.hmcts.sptribs.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.sptribs.common.repositories.model.CorrespondenceDocumentEntity;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;

import java.util.UUID;

@Repository
public interface CorrespondenceDocumentRepository extends JpaRepository<CorrespondenceDocumentEntity, Integer> {

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO correspondence_document (document_id, correspondence_id)
        VALUES (:documentId, :correspondenceId)
        """, nativeQuery = true)
    void insert(@Param("documentId") Long documentId,
                @Param("correspondenceId") UUID correspondenceId);

}
