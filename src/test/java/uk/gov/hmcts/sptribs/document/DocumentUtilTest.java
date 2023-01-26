package uk.gov.hmcts.sptribs.document;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.document.model.ConfidentialDocumentsReceived;
import uk.gov.hmcts.sptribs.document.model.DocumentInfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.documentFrom;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.isApplicableForConfidentiality;
import static uk.gov.hmcts.sptribs.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.sptribs.document.model.DocumentType.GENERAL_LETTER;
import static uk.gov.hmcts.sptribs.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_1;
import static uk.gov.hmcts.sptribs.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_2;

@ExtendWith(MockitoExtension.class)
class DocumentUtilTest {

    private static final String DOC_URL = "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003";
    private static final String DOC_BINARY_URL = "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003/binary";
    private static final String PDF_FILENAME = "draft-divorce-application-1616591401473378.pdf";
    private static final String URL = "url";
    private static final String FILENAME = "filename";
    private static final String BINARY_URL = "binaryUrl";

    @Test
    void shouldConvertFromDocumentInfoToDocument() {
        //When
        final Document document = documentFrom(documentInfo());

        //Then
        assertThat(document)
            .extracting(URL, FILENAME, BINARY_URL)
            .contains(
                DOC_URL,
                PDF_FILENAME,
                DOC_BINARY_URL);
    }

    @Test
    public void isApplicableForConfidentialityShouldReturnTrueForApplicant1WhenGivenDocumentTypeIsApplicableForConfidentiality() {
        assertTrue(isApplicableForConfidentiality(NOTICE_OF_PROCEEDINGS_APP_1, true));
    }

    @Test
    public void isApplicableForConfidentialityShouldReturnTrueForApplicant2WhenGivenDocumentTypeIsApplicableForConfidentiality() {
        assertTrue(isApplicableForConfidentiality(NOTICE_OF_PROCEEDINGS_APP_2, false));
    }

    @Test
    public void isApplicableForConfidentialityShouldReturnTrueWhenGivenDocumentTypeIsApplicableForConfidentiality() {
        assertTrue(isApplicableForConfidentiality(NOTICE_OF_PROCEEDINGS_APP_1, null));
    }

    @Test
    public void isApplicableForConfidentialityShouldReturnTrueForGeneralLetterWhenGivenDocumentTypeIsApplicableForConfidentiality() {
        assertTrue(isApplicableForConfidentiality(GENERAL_LETTER, null));
    }

    @Test
    public void isApplicableForConfidentialityShouldReturnFalseWhenGivenDocumentTypeIsNotApplicableForConfidentiality() {
        assertFalse(isApplicableForConfidentiality(APPLICATION, null));
    }

    @Test
    public void isApplicableForConfidentialityShouldReturnTrueForApplicant1WhenConfidentialDocumentReceivedIsApplicableForConfidentiality(

    ) {
        assertTrue(isApplicableForConfidentiality(ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_1, true));
    }

    @Test
    public void isApplicableForConfidentialityShouldReturnTrueForApplicant2WhenConfidentialDocumentReceivedIsApplicableForConfidentiality(

    ) {
        assertTrue(isApplicableForConfidentiality(ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_2, false));
    }

    private DocumentInfo documentInfo() {
        return new DocumentInfo(
            DOC_URL,
            PDF_FILENAME,
            DOC_BINARY_URL
        );
    }
}
