package uk.gov.hmcts.sptribs.document;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class DocumentFileTypes {

    public static final Set<String> AUDIO_VIDEO_EXTENSIONS = Set.of("mp3", "m4a", "mp4");

    public static final Set<String> CASEWORKER_DOCUMENT_EXTENSIONS = combine(
        Set.of("pdf", "csv", "txt", "rtf", "xlsx", "docx", "doc", "xls"),
        AUDIO_VIDEO_EXTENSIONS
    );

    public static final Set<String> CASEWORKER_UPLOAD_EXTENSIONS = combine(
        CASEWORKER_DOCUMENT_EXTENSIONS,
        Set.of("jpg", "jpeg", "bmp", "tif", "tiff", "png")
    );

    public static final Set<String> BUNDLE_DOCUMENT_EXTENSIONS = Set.of(
        "pdf", "txt", "xlsx", "docx", "doc", "xls", "jpg", "jpeg", "tiff", "bmp", "gif", "svg", "png"
    );

    public static final Set<String> HEARING_RECORDING_EXTENSIONS = Set.of("mp3");

    private DocumentFileTypes() {
    }

    public static boolean isAudioOrVideo(String filename) {
        return isValid(filename, AUDIO_VIDEO_EXTENSIONS);
    }

    public static boolean isValid(String filename, String validExtensions) {
        if (validExtensions == null) {
            return false;
        }

        return isValid(filename, Arrays.stream(validExtensions.split(","))
            .map(String::trim)
            .filter(extension -> !extension.isEmpty())
            .toList());
    }

    public static boolean isValid(String filename, Collection<String> validExtensions) {
        if (filename == null || validExtensions == null) {
            return false;
        }

        int extensionSeparator = filename.lastIndexOf('.');
        if (extensionSeparator < 0 || extensionSeparator == filename.length() - 1) {
            return false;
        }

        String extension = filename.substring(extensionSeparator + 1).toLowerCase(Locale.ROOT);
        return validExtensions.contains(extension);
    }

    private static Set<String> combine(Set<String> first, Set<String> second) {
        Set<String> combined = new HashSet<>(first);
        combined.addAll(second);
        return Set.copyOf(combined);
    }
}
