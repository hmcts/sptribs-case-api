package uk.gov.hmcts.sptribs.document.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.IntegrationTestBase;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentType;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.manager.CaseDataITManager;
import uk.gov.hmcts.sptribs.manager.CaseDocumentITManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentsServiceIT extends IntegrationTestBase {

    private static final long CASE_ID = 123L;
    private static final String BINARY_URL = "documents/recording/binary";

    @Autowired
    private DocumentsService documentsService;

    @Autowired
    private CaseDataITManager caseDataITManager;

    @Autowired
    private CaseDocumentITManager caseDocumentITManager;

    @BeforeEach
    void setUp() {
        caseDataITManager.addCaseData(CASE_ID, "test", "{}");
    }

    @Test
    void shouldIgnoreDuplicateDocumentsWithoutAnEnclosingServiceTransaction() {
        List<ListValue<CaseworkerCICDocument>> documents = List.of(ListValue.<CaseworkerCICDocument>builder()
            .value(CaseworkerCICDocument.builder()
                .documentCategory(DocumentType.LINKED_DOCS)
                .documentLink(Document.builder()
                    .url("documents/recording")
                    .binaryUrl(BINARY_URL)
                    .filename("recording.mp3")
                    .build())
                .build())
            .build());

        List<String> firstSaveErrors = documentsService.saveDocuments(CASE_ID, documents, CaseDocumentType.HEARING_RECORD);
        List<String> duplicateSaveErrors = documentsService.saveDocuments(CASE_ID, documents, CaseDocumentType.HEARING_RECORD);

        assertThat(firstSaveErrors).isEmpty();
        assertThat(duplicateSaveErrors).isEmpty();
        assertThat(caseDocumentITManager.getCount(BINARY_URL)).isEqualTo(1);
    }
}
