package uk.gov.hmcts.sptribs.document;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentFileTypesTest {

    @ParameterizedTest
    @ValueSource(strings = {"recording.mp3", "recording.MP3", "recording.m4a", "recording.MP4"})
    void shouldRecogniseAllSupportedAudioAndVideoExtensions(String filename) {
        assertThat(DocumentFileTypes.isAudioOrVideo(filename)).isTrue();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"document.pdf", "recording.wav", "recording", "recording."})
    void shouldRejectUnsupportedAudioAndVideoExtensions(String filename) {
        assertThat(DocumentFileTypes.isAudioOrVideo(filename)).isFalse();
    }
}
