package uk.gov.hmcts.sptribs.common.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.sptribs.document.model.ContactPartyDocumentDetails;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
import uk.gov.hmcts.sptribs.notification.model.Party;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentsRepository extends JpaRepository<DocumentEntity, Integer> {

    @Query("""
        select d.id
        from DocumentEntity d
        where d.documentBinaryUrl in :documentUrls
        """)
    List<Long> findIdsByDocumentBinaryUrls(
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

    @Modifying
    @Query("""
            DELETE
            FROM DocumentEntity d
            WHERE d.documentBinaryUrl = :documentBinaryUrl
        """)
    void deleteEntryByBinaryURL(
        @Param("documentBinaryUrl") String documentBinaryUrl);

    @Query("""
    select new uk.gov.hmcts.sptribs.document.model.ContactPartyDocumentDetails(
        d,
        max(c.sentOn)
    )
    from DocumentEntity d
        join CorrespondenceDocumentEntity cd
            on cd.id.documentId = d.id
        join CorrespondenceEntity c
            on c.id = cd.id.correspondenceId
    where d.caseReferenceNumber = :caseReference
        and c.receivingParty in :parties
    group by d
    order by d.savedAt desc
    """)
    List<ContactPartyDocumentDetails> findContactPartyDocuments(
        @Param("caseReference") Long caseReference,
        @Param("parties") Collection<Party> parties
    );

    @Query("""
    select d
    from DocumentEntity d
    where d.caseReferenceNumber = :caseReference
        and d.caseDocumentTypeId in :caseDocumentIds
    order by d.savedAt desc
    """)
    List<DocumentEntity> findOrderAndDecisionDocuments(
        Long caseReference,
        Collection<Long> caseDocumentIds
    );

    @Query("""
    select d
    from DocumentEntity d
    where d.caseReferenceNumber = :caseReference
        and d.caseDocumentTypeId = :bundleCategoryId
    order by d.savedAt desc
    """)
    Optional<DocumentEntity> findLatestBundleDocument(
        Long caseReference,
        Long bundleCategoryId
    );
}
