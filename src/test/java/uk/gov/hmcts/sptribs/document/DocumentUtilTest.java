package uk.gov.hmcts.sptribs.document;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentInfo;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.DOCUMENT_VALIDATION_MESSAGE;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.documentFrom;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCICDocumentListWithInvalidFileFormat;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCaseworkerCICDocumentListWithFileFormat;

@ExtendWith(MockitoExtension.class)
class DocumentUtilTest {

    private static final String DOC_URL = "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003";
    private static final String DOC_BINARY_URL = "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003/binary";
    private static final String PDF_FILENAME = "draft-divorce-application-1616591401473378.pdf";
    private static final String CATEGORY_ID_VAL = "A";
    private static final String URL = "url";
    private static final String FILENAME = "filename";
    private static final String BINARY_URL = "binaryUrl";
    private static final String CATEGORY_ID = "categoryId";

    @Test
    void shouldConvertFromDocumentInfoToDocument() {
        //When
        final Document document = documentFrom(documentInfo());

        //Then
        assertThat(document)
            .extracting(URL, FILENAME, BINARY_URL, CATEGORY_ID)
            .contains(
                DOC_URL,
                PDF_FILENAME,
                DOC_BINARY_URL,
                CATEGORY_ID_VAL);
    }

    @Test
    void shouldValidateCICDocumentFormat() {
        //When
        List<ListValue<CICDocument>> documentList = getCICDocumentListWithInvalidFileFormat();
        List<String> errors = DocumentUtil.validateDocumentFormat(documentList);

        //Then
        assertThat(errors).contains(DOCUMENT_VALIDATION_MESSAGE);
    }

    @Test
    void shouldValidateCaseworkerCICDocumentFormatValid() {
        //When
        List<ListValue<CaseworkerCICDocument>> documentList = getCaseworkerCICDocumentListWithFileFormat("docx");
        List<String> errors = DocumentUtil.validateCaseworkerCICDocumentFormat(documentList);

        //Then
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldValidateCaseworkerCICDocumentFormatInvalid() {
        //When
        List<ListValue<CaseworkerCICDocument>> documentList = getCaseworkerCICDocumentListWithFileFormat("xml");
        List<String> errors = DocumentUtil.validateCaseworkerCICDocumentFormat(documentList);

        //Then
        assertThat(errors).contains(DOCUMENT_VALIDATION_MESSAGE);
    }

    @Test
    void shouldValidateDecisionDocumentFormat() {
        //When
        final CICDocument document = CICDocument.builder()
            .documentLink(Document.builder().filename("file.txt").build())
            .documentEmailContent("some email content")
            .build();

        List<String> errors = DocumentUtil.validateDecisionDocumentFormat(document);

        //Then
        assertThat(errors).contains(DOCUMENT_VALIDATION_MESSAGE);
    }

    private DocumentInfo documentInfo() {
        return new DocumentInfo(
            DOC_URL,
            PDF_FILENAME,
            DOC_BINARY_URL,
            CATEGORY_ID_VAL
        );
    }

}
