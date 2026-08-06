package uk.gov.hmcts.sptribs.systemupdate.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.common.repositories.DocumentsRepository;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentType;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.document.service.CaseDocumentTypesCache;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class MigrationDocumentServiceTest {

    @InjectMocks
    private MigrationDocumentService documentsService;

    @Mock
    private DocumentsRepository documentsRepository;

    @Mock
    private CaseDocumentTypesCache caseDocumentTypesCache;

    private static final DocumentType HOSPITAL_RECORDS = DocumentType.HOSPITAL_RECORDS;

    private static final LocalDateTime dateTime = LocalDateTime.now();

    @Test
    void shouldBuildAndSaveNewCaseworkerDocumentEntity() {
        Document evidenceDocument = buildDocument(HOSPITAL_RECORDS.getCategory());

        when(caseDocumentTypesCache.getId(CaseDocumentType.DOCUMENT_MANAGEMENT)).thenReturn(2L);

        documentsService.buildAndSaveNewDocumentEntityWithDocDateTime(evidenceDocument, TEST_CASE_ID, HOSPITAL_RECORDS,
            CaseDocumentType.DOCUMENT_MANAGEMENT, dateTime);

        verify(documentsRepository, times(1)).insertIgnoreDuplicate(
            eq(TEST_CASE_ID),
            eq(evidenceDocument.getUrl()),
            eq(evidenceDocument.getFilename()),
            eq(evidenceDocument.getBinaryUrl()),
            eq(HOSPITAL_RECORDS.name()),
            eq(2L),
            any()
        );
    }

    @Test
    void shouldBuildAndSaveNewBundleDocument() {
        Document bundleDocument = buildDocument(null);

        when(caseDocumentTypesCache.getId(CaseDocumentType.BUNDLE)).thenReturn(9L);

        documentsService.buildAndSaveNewDocumentEntityWithDocDateTime(bundleDocument, TEST_CASE_ID, null,
            CaseDocumentType.BUNDLE, dateTime);

        verify(documentsRepository, times(1)).insertIgnoreDuplicate(
            eq(TEST_CASE_ID),
            eq(bundleDocument.getUrl()),
            eq(bundleDocument.getFilename()),
            eq(bundleDocument.getBinaryUrl()),
            isNull(),
            eq(9L),
            any()
        );
    }

    @Test
    void shouldBuildAndSaveNewDraftOrderDocumentEntity() {
        Document draftEvidenceDocument = buildDocument(HOSPITAL_RECORDS.getCategory());

        when(caseDocumentTypesCache.getId(CaseDocumentType.DRAFT_ORDER)).thenReturn(4L);

        documentsService.buildAndSaveNewDocumentEntityWithDocDateTime(draftEvidenceDocument, TEST_CASE_ID, HOSPITAL_RECORDS,
            CaseDocumentType.DRAFT_ORDER, dateTime);
        verify(documentsRepository, times(1)).insertIgnoreDuplicate(
            eq(TEST_CASE_ID),
            eq(draftEvidenceDocument.getUrl()),
            eq(draftEvidenceDocument.getFilename()),
            eq(draftEvidenceDocument.getBinaryUrl()),
            eq(HOSPITAL_RECORDS.name()),
            eq(4L),
            any()
        );
    }

    @Test
    void shouldReturnLogIfDocumentAlreadyExistsInDatabase() {
        Logger logger = (Logger) LoggerFactory.getLogger(MigrationDocumentService.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        Document evidenceDocument = buildDocument(HOSPITAL_RECORDS.getCategory());

        when(caseDocumentTypesCache.getId(CaseDocumentType.DOCUMENT_MANAGEMENT)).thenReturn(2L);
        when(documentsRepository.insertIgnoreDuplicate(
            anyLong(),
            anyString(),
            anyString(),
            anyString(),
            any(),
            anyLong(),
            any()
        )).thenReturn(0);

        documentsService.buildAndSaveNewDocumentEntityWithDocDateTime(evidenceDocument, TEST_CASE_ID, HOSPITAL_RECORDS,
            CaseDocumentType.DOCUMENT_MANAGEMENT, dateTime);

        assertThat(listAppender.list)
            .anyMatch(event ->
                event.getFormattedMessage().contains("Document already exists in document table")
                    && event.getLevel() == Level.INFO

            );
    }

    private Document buildDocument(String categoryId) {
        return Document.builder()
            .url("example.com/test-document.pdf")
            .filename("test-document.pdf")
            .binaryUrl("example.com/test-document.pdf/binary")
            .categoryId(categoryId)
            .build();
    }

}
