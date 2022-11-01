package uk.gov.hmcts.sptribs.document;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.GeneralParties;
import uk.gov.hmcts.sptribs.document.model.ConfidentialDocumentsReceived;
import uk.gov.hmcts.sptribs.document.model.DivorceDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentInfo;
import uk.gov.hmcts.sptribs.document.print.model.Letter;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.sptribs.ciccase.model.ContactDetailsType.PRIVATE;
import static uk.gov.hmcts.sptribs.ciccase.model.GeneralParties.APPLICANT;
import static uk.gov.hmcts.sptribs.ciccase.model.GeneralParties.RESPONDENT;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.divorceDocumentFrom;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.documentFrom;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.documentsWithDocumentType;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.isApplicableForConfidentiality;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.isConfidential;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.lettersWithDocumentType;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.mapToLetters;
import static uk.gov.hmcts.sptribs.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.sptribs.document.model.DocumentType.D10;
import static uk.gov.hmcts.sptribs.document.model.DocumentType.GENERAL_LETTER;
import static uk.gov.hmcts.sptribs.document.model.DocumentType.MARRIAGE_CERTIFICATE;
import static uk.gov.hmcts.sptribs.document.model.DocumentType.NAME_CHANGE_EVIDENCE;
import static uk.gov.hmcts.sptribs.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_1;
import static uk.gov.hmcts.sptribs.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_2;
import static uk.gov.hmcts.sptribs.document.model.DocumentType.OTHER;

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
    void shouldCreateDivorceDocumentFromDocumentInfoAndDocumentType() {
        //When
        final DivorceDocument divorceDocument = divorceDocumentFrom(documentInfo(), OTHER);

        //Then
        assertThat(divorceDocument.getDocumentType()).isEqualTo(OTHER);
        assertThat(divorceDocument.getDocumentFileName()).isEqualTo(PDF_FILENAME);
        assertThat(divorceDocument
            .getDocumentLink())
            .extracting(URL, FILENAME, BINARY_URL)
            .contains(
                DOC_URL,
                PDF_FILENAME,
                DOC_BINARY_URL);
    }

    @Test
    void shouldReturnListOfLetterOfGivenDocumentTypeIfPresent() {
        //Given
        final ListValue<DivorceDocument> doc1 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();

        final ListValue<DivorceDocument> doc2 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(MARRIAGE_CERTIFICATE)
                .build())
            .build();

        //When
        final List<Letter> letters = lettersWithDocumentType(
            asList(doc1, doc2),
            MARRIAGE_CERTIFICATE);

        //Then
        assertThat(letters.size()).isEqualTo(1);
        assertThat(letters.get(0).getDivorceDocument()).isSameAs(doc2.getValue());
        assertThat(letters.get(0).getCount()).isGreaterThan(0);
    }

    @Test
    void shouldNotFindDocumentOfGivenDocumentTypeIfNotPresent() {
        //Given
        final ListValue<DivorceDocument> doc1 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();

        final ListValue<DivorceDocument> doc2 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(MARRIAGE_CERTIFICATE)
                .build())
            .build();

        //When
        final List<Letter> letters = lettersWithDocumentType(
            asList(doc1, doc2),
            NAME_CHANGE_EVIDENCE);

        //Then
        assertThat(letters.size()).isZero();
    }

    @Test
    void shouldReturnEmptyListIfNullDocumentList() {
        //When
        final List<Letter> letters = lettersWithDocumentType(null, NAME_CHANGE_EVIDENCE);
        //Then
        assertThat(letters.size()).isZero();
    }

    @Test
    void shouldReturnEmptyListIfEmptyDocumentList() {
        //When
        final List<Letter> letters = lettersWithDocumentType(emptyList(), NAME_CHANGE_EVIDENCE);
        //Then
        assertThat(letters.size()).isZero();
    }

    @Test
    void shouldReturnListOfLettersOfGivenDocumentTypesIfPresent() {
        //Given
        final ListValue<DivorceDocument> doc1 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();

        final ListValue<DivorceDocument> doc2 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(MARRIAGE_CERTIFICATE)
                .build())
            .build();

        final ListValue<DivorceDocument> doc3 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(NAME_CHANGE_EVIDENCE)
                .build())
            .build();

        //When
        final List<Letter> letters = lettersWithDocumentType(
            asList(doc1, doc2, doc3),
            NAME_CHANGE_EVIDENCE);

        //Then
        assertThat(letters.size()).isEqualTo(1);
        assertThat(letters.get(0).getDivorceDocument()).isSameAs(doc3.getValue());
    }

    @Test
    void shouldReturnTrueIfDocumentWithTypeD10IsPresent() {
        //Given
        final ListValue<DivorceDocument> d10Document = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(D10)
                .build())
            .build();

        //When
        final boolean d10IsPresent = documentsWithDocumentType(
            singletonList(d10Document),
            D10);

        //Then
        assertThat(d10IsPresent).isTrue();
    }

    @Test
    void shouldReturnFalseIfDocumentWithTypeD10IsNotPresent() {
        //Given
        final ListValue<DivorceDocument> doc1 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();

        //When
        final boolean d10IsPresent = documentsWithDocumentType(
            singletonList(doc1),
            D10);

        //Then
        assertThat(d10IsPresent).isFalse();
    }

    @Test
    void shouldReturnFalseIfNullDocumentList() {
        //When
        final boolean d10IsPresent = documentsWithDocumentType(null, D10);
        //Then
        assertThat(d10IsPresent).isFalse();
    }

    @Test
    void shouldReturnFalseIfEmptyDocumentList() {
        //When
        final boolean d10IsPresent = documentsWithDocumentType(emptyList(), D10);
        //Then
        assertThat(d10IsPresent).isFalse();
    }

    @Test
    void mapToLettersShouldReturnListOfLettersOfGivenDocumentType() {
        //Given
        final ListValue<Document> doc1 = ListValue.<Document>builder()
            .value(Document.builder().filename("doc1.pdf").build())
            .build();

        final ListValue<Document> doc2 = ListValue.<Document>builder()
            .value(Document.builder().filename("doc2.pdf").build())
            .build();

        //When
        final List<Letter> letters = mapToLetters(asList(doc1, doc2), NOTICE_OF_PROCEEDINGS_APP_1);

        //Then
        assertThat(letters.size()).isEqualTo(2);
        assertThat(
            letters.stream().map(letter -> letter.getDivorceDocument().getDocumentFileName()).collect(Collectors.toList()))
            .containsExactlyInAnyOrder("doc1.pdf", "doc2.pdf");
        assertThat(
            letters.stream().map(letter -> letter.getDivorceDocument().getDocumentType()).collect(Collectors.toList()))
            .containsExactlyInAnyOrder(NOTICE_OF_PROCEEDINGS_APP_1, NOTICE_OF_PROCEEDINGS_APP_1);
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

    @Test
    public void isConfidentialShouldReturnTrueWhenDocumentTypeIsGeneralLetterAndGeneralLetterPartyIsApplicant() {
        //When
        var caseData = CaseData.builder().build();
        caseData.getGeneralLetter().setGeneralLetterParties(APPLICANT);
        caseData.getApplicant1().setContactDetailsType(PRIVATE);

        //Then
        assertTrue(isConfidential(caseData, GENERAL_LETTER));
    }

    @Test
    public void isConfidentialShouldReturnTrueWhenDocumentTypeIsGeneralLetterAndGeneralLetterPartyIsRespondent() {
        //When
        var caseData = CaseData.builder().build();
        caseData.getGeneralLetter().setGeneralLetterParties(RESPONDENT);
        caseData.getApplicant2().setContactDetailsType(PRIVATE);

        //Then
        assertTrue(isConfidential(caseData, GENERAL_LETTER));
    }


    @Test
    public void isConfidentialShouldReturnFalseWhenDocumentTypeIsGeneralLetterAndGeneralLetterPartyIsOther() {
        var caseData = CaseData.builder().build();
        caseData.getGeneralLetter().setGeneralLetterParties(GeneralParties.OTHER);
        caseData.getApplicant2().setContactDetailsType(PRIVATE);

        assertFalse(isConfidential(caseData, GENERAL_LETTER));
    }

    private DocumentInfo documentInfo() {
        return new DocumentInfo(
            DOC_URL,
            PDF_FILENAME,
            DOC_BINARY_URL
        );
    }
}
