package uk.gov.hmcts.sptribs.document;

import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.document.model.DocumentInfo;

public final class DocumentUtil {

    private DocumentUtil() {
    }

    public static Document documentFrom(final DocumentInfo documentInfo) {
        return new Document(
            documentInfo.getUrl(),
            documentInfo.getFilename(),
            documentInfo.getBinaryUrl());
    }

}
