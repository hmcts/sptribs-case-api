package uk.gov.hmcts.sptribs.caseworker.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ContactPartiesAllowedFileTypes {

    PDF("pdf"),
    CSV("csv"),
    JSON("json"),
    ODT("odt"),
    TXT("txt"),
    RTF("rtf"),
    XLSX("xlsx"),
    DOC("doc"),
    DOCX("docx");

    private final String fileType;

    public static boolean isFileTypeValid(String fileType) {
        try {
            ContactPartiesAllowedFileTypes.valueOf(fileType.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
