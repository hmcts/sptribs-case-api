package uk.gov.hmcts.sptribs.document;

import com.google.common.collect.Lists;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.document.model.ConfidentialDocumentsReceived;
import uk.gov.hmcts.sptribs.document.model.DocumentInfo;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

import java.util.List;

import static uk.gov.hmcts.sptribs.document.model.DocumentType.GENERAL_LETTER;
import static uk.gov.hmcts.sptribs.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_1;
import static uk.gov.hmcts.sptribs.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_2;

public final class DocumentUtil {

    private DocumentUtil() {
    }

    public static Document documentFrom(final DocumentInfo documentInfo) {
        return new Document(
            documentInfo.getUrl(),
            documentInfo.getFilename(),
            documentInfo.getBinaryUrl());
    }

    public static boolean isApplicableForConfidentiality(final DocumentType documentType, final Boolean isApplicant1) {
        List<DocumentType> documentsForApplicant1 = Lists.newArrayList(NOTICE_OF_PROCEEDINGS_APP_1);

        List<DocumentType> documentsForApplicant2 = Lists.newArrayList(NOTICE_OF_PROCEEDINGS_APP_2);

        List<DocumentType> documentsForBothApplicants = Lists.newArrayList(
            NOTICE_OF_PROCEEDINGS_APP_1,
            NOTICE_OF_PROCEEDINGS_APP_2,
            GENERAL_LETTER
        );

        return isApplicant1 == null ? documentsForBothApplicants.contains(documentType)
            : isApplicant1 ? documentsForApplicant1.contains(documentType) : documentsForApplicant2.contains(documentType);
    }

    public static boolean isApplicableForConfidentiality(final ConfidentialDocumentsReceived documentType, final Boolean isApplicant1) {
        List<ConfidentialDocumentsReceived> documentsForApplicant1 = Lists.newArrayList(
            ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_1
        );

        List<ConfidentialDocumentsReceived> documentsForApplicant2 = Lists.newArrayList(
            ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_2
        );

        return isApplicant1 ? documentsForApplicant1.contains(documentType) : documentsForApplicant2.contains(documentType);
    }

    public static ConfidentialDocumentsReceived getConfidentialDocumentType(final DocumentType documentType) {
        return NOTICE_OF_PROCEEDINGS_APP_1.equals(documentType)
            ? ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_1
            : ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_2;
    }
}
