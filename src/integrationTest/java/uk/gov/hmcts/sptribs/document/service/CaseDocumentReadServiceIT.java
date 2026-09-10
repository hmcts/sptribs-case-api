package uk.gov.hmcts.sptribs.document.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.IntegrationTestBase;
import uk.gov.hmcts.sptribs.document.exception.DocumentSelectionException;
import uk.gov.hmcts.sptribs.document.model.BundleDocumentsView;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentType;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentView;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.document.model.SelectedCaseDocuments;
import uk.gov.hmcts.sptribs.manager.CaseDataITManager;
import uk.gov.hmcts.sptribs.manager.CaseDocumentITManager;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CaseDocumentReadServiceIT extends IntegrationTestBase {

    private static final long CASE_REFERENCE = 123L;
    private static final long OTHER_CASE_REFERENCE = 456L;

    @Autowired
    private CaseDocumentReadService caseDocumentReadService;

    @Autowired
    private CaseDocumentTypesCache caseDocumentTypesCache;

    @Autowired
    private CaseDataITManager caseDataITManager;

    @Autowired
    private CaseDocumentITManager caseDocumentITManager;

    @BeforeEach
    void setUp() {
        caseDataITManager.addCaseData(CASE_REFERENCE, "test", "{}");
        caseDataITManager.addCaseData(2L, OTHER_CASE_REFERENCE, "test", "{}");
    }

    @Test
    void shouldProjectOnlyEligibleDocumentsForRequestedCaseFromDatabase() {
        addDatabaseDocument(CASE_REFERENCE, "db-only.pdf", CaseDocumentType.DOCUMENT_MANAGEMENT);
        addDatabaseDocument(CASE_REFERENCE, "correspondence.pdf", CaseDocumentType.CORRESPONDENCE);
        addDatabaseDocument(OTHER_CASE_REFERENCE, "other-case.pdf", CaseDocumentType.DOCUMENT_MANAGEMENT);

        List<ListValue<CaseDocumentView>> result =
            caseDocumentReadService.getCaseViewDocuments(CASE_REFERENCE);

        assertThat(result)
            .extracting(document -> document.getValue().getDocumentLink().getFilename())
            .containsExactly("db-only.pdf")
            .doesNotContain("correspondence.pdf", "other-case.pdf");
    }

    @Test
    void shouldClassifyDatabaseDocumentAsInitialUsingOnlyItsBinaryUrl() {
        UUID databaseDocumentId = addDatabaseDocument(
            CASE_REFERENCE,
            "db-only.pdf",
            CaseDocumentType.DOCUMENT_MANAGEMENT
        );

        BundleDocumentsView result = caseDocumentReadService.getBundleDocuments(
            CASE_REFERENCE,
            Set.of(documentUrl(databaseDocumentId) + "/binary")
        );

        assertThat(result.getAllDocuments())
            .extracting(document -> document.getDocumentLink().getFilename())
            .containsExactly("db-only.pdf");
        assertThat(result.getInitialDocuments())
            .extracting(document -> document.getDocumentLink().getFilename())
            .containsExactly("db-only.pdf");
    }

    @Test
    void shouldResolveSelectionsWithinRequestedCase() {
        UUID databaseDocumentId = addDatabaseDocument(
            CASE_REFERENCE,
            "db-only.pdf",
            CaseDocumentType.DOCUMENT_MANAGEMENT
        );

        DynamicMultiSelectList selection = DynamicMultiSelectList.builder()
            .value(List.of(option(databaseDocumentId)))
            .build();

        SelectedCaseDocuments selectedDocuments = caseDocumentReadService.getSelectedContactPartyDocuments(
            CASE_REFERENCE,
            selection,
            10
        );

        assertThat(selectedDocuments.getDocuments())
            .extracting(document -> document.getDocumentLink().getFilename())
            .containsExactly("db-only.pdf");
    }

    @Test
    void shouldRejectSelectionFromAnotherCase() {
        UUID otherCaseDocumentId = addDatabaseDocument(
            OTHER_CASE_REFERENCE,
            "other-case.pdf",
            CaseDocumentType.DOCUMENT_MANAGEMENT
        );
        DynamicMultiSelectList selection = DynamicMultiSelectList.builder()
            .value(List.of(option(otherCaseDocumentId)))
            .build();

        assertThatThrownBy(() -> caseDocumentReadService.getSelectedContactPartyDocuments(
            CASE_REFERENCE,
            selection,
            10
        )).isInstanceOf(DocumentSelectionException.class)
            .hasMessageContaining(otherCaseDocumentId.toString())
            .hasMessageContaining(String.valueOf(CASE_REFERENCE));
    }

    private DynamicListElement option(UUID documentId) {
        return DynamicListElement.builder()
            .code(documentId)
            .label("label is not parsed")
            .build();
    }

    private UUID addDatabaseDocument(
        long caseReference,
        String filename,
        CaseDocumentType caseDocumentType
    ) {
        UUID documentId = UUID.randomUUID();
        caseDocumentITManager.addCaseDocument(
            caseReference,
            documentUrl(documentId),
            documentUrl(documentId) + "/binary",
            filename,
            DocumentType.LINKED_DOCS.name(),
            caseDocumentTypesCache.getId(caseDocumentType),
            OffsetDateTime.now()
        );
        return documentId;
    }

    private String documentUrl(UUID documentId) {
        return "http://document-management/documents/" + documentId;
    }
}
