package uk.gov.hmcts.sptribs.common.repositories.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.sptribs.IntegrationTestBase;
import uk.gov.hmcts.sptribs.common.repositories.DocumentsRepository;
import uk.gov.hmcts.sptribs.document.model.ContactPartyDocumentDetails;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
import uk.gov.hmcts.sptribs.manager.CaseCorrespondenceManager;
import uk.gov.hmcts.sptribs.manager.CaseDataManager;
import uk.gov.hmcts.sptribs.manager.CaseDocumentManager;
import uk.gov.hmcts.sptribs.manager.CorrespondenceDocumentManager;
import uk.gov.hmcts.sptribs.notification.model.Party;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.camunda.bpm.model.xml.test.assertions.ModelAssertions.assertThat;

@Transactional
class DocumentsRepositoryImplIT extends IntegrationTestBase {

    @Autowired
    private DocumentsRepository repository;

    @Autowired
    private CaseDataManager caseDataManager;

    @Autowired
    private CaseDocumentManager caseDocumentManager;

    @Autowired
    private CaseCorrespondenceManager caseCorrespondenceManager;

    @Autowired
    private CorrespondenceDocumentManager correspondenceDocumentManager;

    private static final long APPLICATION_TYPE_ID = 1L;
    private static final long DOCUMENT_MANAGEMENT_TYPE_ID = 2L;
    private static final long ORDER_TYPE_ID = 3L;
    private static final long DRAFT_ORDER_ID = 4L;
    private static final long DECISION_ID = 5L;
    private static final long FINAL_DECISION_ID = 6L;
    private static final long BUNDLE_ID = 9L;

    @BeforeEach
    void setUp() {
        caseDataManager.addCaseData(123L, "test", "{}");
    }

    @Test
    void shouldDeleteDocumentByBinaryUrl() {

        String binaryUrl = UUID.randomUUID().toString();

        caseDocumentManager.addCaseDocument(123L, binaryUrl, DOCUMENT_MANAGEMENT_TYPE_ID, OffsetDateTime.now());

        assertThat(caseDocumentManager.getCount(binaryUrl)).isEqualTo(1);

        repository.deleteEntryByBinaryURL(binaryUrl);

        assertThat(caseDocumentManager.getCount(binaryUrl)).isZero();
    }

    @Test
    void shouldFindIdsByDocumentBinaryUrls() {
        //given
        String binaryUrl1 = UUID.randomUUID().toString();
        String binaryUrl2 = UUID.randomUUID().toString();

        Long documentId1 = caseDocumentManager.addCaseDocument(123L, binaryUrl1, DOCUMENT_MANAGEMENT_TYPE_ID,
            OffsetDateTime.now());
        Long documentId2 = caseDocumentManager.addCaseDocument(123L, binaryUrl2, DOCUMENT_MANAGEMENT_TYPE_ID,
            OffsetDateTime.now());

        //when
        List<Long> ids = repository.findIdsByDocumentBinaryUrls(
            List.of(binaryUrl1, binaryUrl2)
        );

        //then
        assertThat(ids)
            .containsExactlyInAnyOrder(documentId1, documentId2);

    }

    @Test
    void shouldUpdateDocumentTypeName() {
        //given
        String binaryUrl = UUID.randomUUID().toString();

        caseDocumentManager.addCaseDocument(123L, binaryUrl, DOCUMENT_MANAGEMENT_TYPE_ID, OffsetDateTime.now());

        //when
        repository.setDocumentTypeNameByDocumentBinaryUrl(
            binaryUrl,
            "ORDER"
        );

        //then
        DocumentEntity entity =
            caseDocumentManager.findByBinaryUrl(binaryUrl);

        assertThat(entity.getDocumentTypeName())
            .isEqualTo("ORDER");

        assertThat(entity.getUpdatedAt())
            .isNotNull();
    }

    @Test
    void shouldUpdateCaseDocumentTypeId() {
        //given
        String binaryUrl = UUID.randomUUID().toString();

        caseDocumentManager.addCaseDocument(123L, binaryUrl, DRAFT_ORDER_ID, OffsetDateTime.now());

        //when
        repository.updateCaseDocumentTypeIdByDocumentBinaryUrl(
            binaryUrl,
            ORDER_TYPE_ID
        );

        //then
        DocumentEntity entity =
            caseDocumentManager.findByBinaryUrl(binaryUrl);

        assertThat(entity.getCaseDocumentTypeId())
            .isEqualTo(ORDER_TYPE_ID);

        assertThat(entity.getUpdatedAt())
            .isNotNull();

    }

    @Test
    void shouldFindOrderAndDecisionDocuments() {

        //given
        String binaryUrl1 = UUID.randomUUID().toString();
        String binaryUrl2 = UUID.randomUUID().toString();
        String binaryUrl3 = UUID.randomUUID().toString();
        String binaryUrl4 = UUID.randomUUID().toString();

        caseDocumentManager.addCaseDocument(123L, binaryUrl1, APPLICATION_TYPE_ID, OffsetDateTime.now());
        caseDocumentManager.addCaseDocument(123L, binaryUrl2, ORDER_TYPE_ID, OffsetDateTime.now());
        caseDocumentManager.addCaseDocument(123L, binaryUrl3, DECISION_ID, OffsetDateTime.now().plusHours(1));
        caseDocumentManager.addCaseDocument(123L, binaryUrl4, FINAL_DECISION_ID, OffsetDateTime.now().plusHours(2));

        //when
        List<DocumentEntity> orderAndDecisionDocs =
            repository.findOrderAndDecisionDocuments(123L, List.of(3L, 5L, 6L));

        //then
        assertThat(orderAndDecisionDocs).hasSize(3);

        assertThat(orderAndDecisionDocs)
            .extracting(DocumentEntity::getDocumentBinaryUrl)
            .containsExactly(
                binaryUrl4,
                binaryUrl3,
                binaryUrl2
            );
    }

    @Test
    void shouldFindLatestBundleDocument() {

        //given
        String binaryUrl1 = UUID.randomUUID().toString();
        String binaryUrl2 = UUID.randomUUID().toString();

        caseDocumentManager.addCaseDocument(123L, binaryUrl1, BUNDLE_ID, OffsetDateTime.now());
        caseDocumentManager.addCaseDocument(123L, binaryUrl2, BUNDLE_ID, OffsetDateTime.now().plusHours(1));

        //when
        Optional<DocumentEntity> result =
            repository.findFirstByCaseReferenceNumberAndCaseDocumentTypeIdOrderBySavedAtDesc(
                123L,
                BUNDLE_ID
            );

        //then
        assertThat(result).isPresent();
        assertThat(result.get().getDocumentBinaryUrl())
            .isEqualTo(binaryUrl2);

    }

    @Test
    void shouldFindContactPartyDocuments() {
        //given
        String binaryUrl = UUID.randomUUID().toString();
        Long documentId = caseDocumentManager.addCaseDocument(
            123L,
            binaryUrl,
            DOCUMENT_MANAGEMENT_TYPE_ID,
            OffsetDateTime.now()
        );

        UUID correspondenceId = UUID.randomUUID();

        caseCorrespondenceManager.addCorrespondence(
            correspondenceId,
            123L,
            Party.APPLICANT,
            OffsetDateTime.now().plusHours(1)
        );

        UUID correspondenceIdTribunal = UUID.randomUUID();

        caseCorrespondenceManager.addCorrespondence(
            correspondenceIdTribunal,
            123L,
            Party.TRIBUNAL,
            OffsetDateTime.now().plusHours(1)
        );

        correspondenceDocumentManager.linkDocument(
            correspondenceId,
            documentId
        );

        correspondenceDocumentManager.linkDocument(
            correspondenceIdTribunal,
            documentId
        );

        //when
        List<ContactPartyDocumentDetails> result =
            repository.findContactPartyDocuments(
                123L,
                List.of( Party.APPLICANT, Party.REPRESENTATIVE, Party.SUBJECT),
                List.of(ORDER_TYPE_ID, DECISION_ID, FINAL_DECISION_ID)
            );

        //then
        assertThat(result).hasSize(1);
        ContactPartyDocumentDetails details = result.getFirst();

        assertThat(details.document().getDocumentBinaryUrl()).isEqualTo(binaryUrl);
        assertThat(details.sentOn()).isEqualTo(OffsetDateTime.now().plusHours(1));
    }
}
