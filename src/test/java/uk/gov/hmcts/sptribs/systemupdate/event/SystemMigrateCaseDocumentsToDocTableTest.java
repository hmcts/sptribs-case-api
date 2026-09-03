package uk.gov.hmcts.sptribs.systemupdate.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.DocumentManagement;
import uk.gov.hmcts.sptribs.caseworker.model.HearingSummary;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.repositories.exception.document.DocumentSaveException;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentType;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.systemupdate.service.MigrationDocumentService;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class SystemMigrateCaseDocumentsToDocTableTest {

    @InjectMocks
    private SystemMigrateCaseDocumentsToDocTable migrationEvent;

    @Mock
    private MigrationDocumentService migrationDocumentService;

    @Test
    void shouldMigrateEvidenceAndHearingMediaWithTheirCaseDocumentTypes() {
        LocalDate hearingDate = LocalDate.of(2025, 2, 5);
        CaseworkerCICDocument evidence = document("evidence.mp4", "evidence-binary");
        CaseworkerCICDocument hearingRecord = document("hearing.mp3", "hearing-binary");

        Listing listing = Listing.builder()
            .date(hearingDate)
            .summary(HearingSummary.builder()
                .recFile(List.of(listValue(hearingRecord)))
                .build())
            .build();
        CaseData caseData = CaseData.builder()
            .allDocManagement(DocumentManagement.builder()
                .caseworkerCICDocument(List.of(listValue(evidence)))
                .build())
            .hearingList(List.of(new ListValue<>("hearing-1", listing)))
            .build();
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        migrationEvent.aboutToSubmit(caseDetails, null);

        verify(migrationDocumentService).buildAndSaveNewDocumentEntityWithDocDateTime(
            any(Document.class),
            eq(TEST_CASE_ID),
            eq(DocumentType.LINKED_DOCS),
            eq(CaseDocumentType.HEARING_RECORD),
            eq(hearingDate.atStartOfDay())
        );
        verify(migrationDocumentService).buildAndSaveNewDocumentEntityWithDocDateTime(
            any(Document.class),
            eq(TEST_CASE_ID),
            eq(DocumentType.LINKED_DOCS),
            eq(CaseDocumentType.DOCUMENT_MANAGEMENT),
            isNull()
        );
        verifyNoMoreInteractions(migrationDocumentService);
    }

    @Test
    void shouldPreferHearingRecordWhenBinaryUrlIsDuplicated() {
        CaseworkerCICDocument hearingRecord = document("hearing.mp3", "duplicate-binary");
        CaseworkerCICDocument evidence = document("evidence.mp4", "duplicate-binary");
        Listing listing = Listing.builder()
            .date(LocalDate.of(2025, 2, 5))
            .summary(HearingSummary.builder().recFile(List.of(listValue(hearingRecord))).build())
            .build();
        CaseData caseData = CaseData.builder()
            .allDocManagement(DocumentManagement.builder()
                .caseworkerCICDocument(List.of(listValue(evidence)))
                .build())
            .hearingList(List.of(new ListValue<>("hearing-1", listing)))
            .build();
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        migrationEvent.aboutToSubmit(caseDetails, null);

        verify(migrationDocumentService).buildAndSaveNewDocumentEntityWithDocDateTime(
            any(Document.class),
            eq(TEST_CASE_ID),
            eq(DocumentType.LINKED_DOCS),
            eq(CaseDocumentType.HEARING_RECORD),
            eq(LocalDate.of(2025, 2, 5).atStartOfDay())
        );
        verifyNoMoreInteractions(migrationDocumentService);
    }

    @Test
    void shouldFailTheEventWhenADocumentCannotBeSaved() {
        CaseworkerCICDocument evidence = document("evidence.mp4", "evidence-binary");
        CaseData caseData = CaseData.builder()
            .allDocManagement(DocumentManagement.builder()
                .caseworkerCICDocument(List.of(listValue(evidence)))
                .build())
            .build();
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        doThrow(new DocumentSaveException("Database error", new RuntimeException()))
            .when(migrationDocumentService)
            .buildAndSaveNewDocumentEntityWithDocDateTime(
                any(Document.class), anyLong(), any(DocumentType.class), any(CaseDocumentType.class), isNull()
            );

        assertThatThrownBy(() -> migrationEvent.aboutToSubmit(caseDetails, null))
            .isInstanceOf(DocumentSaveException.class)
            .hasMessageContaining("Failed to save 1 document(s)");
    }

    private static CaseworkerCICDocument document(String filename, String binaryUrl) {
        return CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentLink(Document.builder()
                .url(binaryUrl)
                .binaryUrl(binaryUrl)
                .filename(filename)
                .build())
            .build();
    }

    private static ListValue<CaseworkerCICDocument> listValue(CaseworkerCICDocument document) {
        return new ListValue<>("document-1", document);
    }
}
