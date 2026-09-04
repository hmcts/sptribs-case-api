package uk.gov.hmcts.sptribs.caseworker.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentType;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.document.service.DocumentsService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.sptribs.document.model.CaseDocumentType.HEARING_RECORD;

@ExtendWith(MockitoExtension.class)
class HearingRecordingDocumentSaverTest {

    private static final long CASE_ID = 123L;

    @Mock
    private DocumentsService documentsService;

    @ParameterizedTest
    @ValueSource(strings = {"recording.mp3", "recording.m4a", "recording.mp4"})
    void shouldSaveAudioAndVideoRecordingAsHearingRecord(String filename) {
        Document recording = Document.builder()
            .url("document-url")
            .binaryUrl("document-binary-url")
            .filename(filename)
            .build();
        List<ListValue<CaseworkerCICDocument>> recordings = List.of(ListValue.<CaseworkerCICDocument>builder()
            .value(CaseworkerCICDocument.builder()
                .documentLink(recording)
                .documentCategory(DocumentType.LINKED_DOCS)
                .build())
            .build());
        List<String> errors = new ArrayList<>();

        HearingRecordingDocumentSaver.save(CASE_ID, recordings, documentsService, errors);

        verify(documentsService).buildAndSaveNewDocumentEntity(
            recording, CASE_ID, DocumentType.LINKED_DOCS, HEARING_RECORD);
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldReturnAnErrorWhenRecordingCannotBeSaved() {
        Document failedRecording = Document.builder()
            .url("document-url")
            .binaryUrl("document-binary-url")
            .filename("recording.mp3")
            .build();
        Document savedRecording = Document.builder()
            .url("second-document-url")
            .binaryUrl("second-document-binary-url")
            .filename("recording.m4a")
            .build();
        List<ListValue<CaseworkerCICDocument>> recordings = List.of(ListValue.<CaseworkerCICDocument>builder()
            .value(CaseworkerCICDocument.builder()
                .documentLink(failedRecording)
                .documentCategory(DocumentType.LINKED_DOCS)
                .build())
            .build(), ListValue.<CaseworkerCICDocument>builder()
            .value(CaseworkerCICDocument.builder()
                .documentLink(savedRecording)
                .documentCategory(DocumentType.LINKED_DOCS)
                .build())
            .build());
        List<String> errors = new ArrayList<>();
        doThrow(new RuntimeException("database unavailable")).when(documentsService)
            .buildAndSaveNewDocumentEntity(failedRecording, CASE_ID, DocumentType.LINKED_DOCS,
                CaseDocumentType.HEARING_RECORD);

        HearingRecordingDocumentSaver.save(CASE_ID, recordings, documentsService, errors);

        assertThat(errors).hasSize(1);
        verify(documentsService).buildAndSaveNewDocumentEntity(
            savedRecording, CASE_ID, DocumentType.LINKED_DOCS, HEARING_RECORD);
    }
}
