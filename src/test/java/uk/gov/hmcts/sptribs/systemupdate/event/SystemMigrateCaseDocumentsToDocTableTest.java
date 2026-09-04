package uk.gov.hmcts.sptribs.systemupdate.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.HearingSummary;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentType;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.systemupdate.service.MigrationDocumentService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SystemMigrateCaseDocumentsToDocTableTest {

    private static final long CASE_ID = 123L;
    private static final LocalDate HEARING_DATE = LocalDate.of(2025, 2, 5);

    @Mock
    private MigrationDocumentService documentsService;

    @InjectMocks
    private SystemMigrateCaseDocumentsToDocTable systemMigrateCaseDocumentsToDocTable;

    @Test
    void shouldInsertAudioAndVideoHearingRecords() {
        Document audioRecording = recording("audio.mp3", "audio-binary-url");
        Document videoRecording = recording("video.mp4", "video-binary-url");
        Listing hearing = Listing.builder()
            .date(HEARING_DATE)
            .summary(HearingSummary.builder()
                .recFile(List.of(document(audioRecording), document(videoRecording)))
                .build())
            .build();
        CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(CASE_ID);
        details.setData(CaseData.builder()
            .hearingList(List.of(new ListValue<>("hearing-id", hearing)))
            .build());

        systemMigrateCaseDocumentsToDocTable.aboutToSubmit(details, new CaseDetails<>());

        LocalDateTime uploadedAt = HEARING_DATE.atStartOfDay();
        verify(documentsService).buildAndSaveNewDocumentEntityWithDocDateTime(
            audioRecording, CASE_ID, DocumentType.LINKED_DOCS, CaseDocumentType.HEARING_RECORD, uploadedAt);
        verify(documentsService).buildAndSaveNewDocumentEntityWithDocDateTime(
            videoRecording, CASE_ID, DocumentType.LINKED_DOCS, CaseDocumentType.HEARING_RECORD, uploadedAt);
    }

    private static Document recording(String filename, String binaryUrl) {
        return Document.builder()
            .url("document-url")
            .binaryUrl(binaryUrl)
            .filename(filename)
            .build();
    }

    private static ListValue<CaseworkerCICDocument> document(Document document) {
        return ListValue.<CaseworkerCICDocument>builder()
            .value(CaseworkerCICDocument.builder()
                .documentCategory(DocumentType.LINKED_DOCS)
                .documentLink(document)
                .build())
            .build();
    }
}
