package uk.gov.hmcts.sptribs.caseworker.service.print;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CaseDocuments;
import uk.gov.hmcts.sptribs.document.model.DivorceDocument;
import uk.gov.hmcts.sptribs.document.print.BulkPrintService;
import uk.gov.hmcts.sptribs.document.print.model.Print;

import java.time.LocalDate;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.document.model.DocumentType.AOS_OVERDUE_LETTER;
import static uk.gov.hmcts.sptribs.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.sptribs.document.model.DocumentType.NAME_CHANGE_EVIDENCE;
import static uk.gov.hmcts.sptribs.document.model.DocumentType.RESPONDENT_ANSWERS;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class AosOverduePrinterTest {

    @Mock
    private BulkPrintService bulkPrintService;

    @InjectMocks
    private AosOverduePrinter aosOverduePrinter;

    @Captor
    private ArgumentCaptor<Print> printCaptor;

    private static final LocalDate NOW = LocalDate.now();

    @Test
    void shouldPrintAosOverdueLetterForApplicantIfRequiredDocumentsArePresent() {
        //Given
        final ListValue<DivorceDocument> doc1 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(AOS_OVERDUE_LETTER)
                .documentDateAdded(NOW)
                .build())
            .build();

        final ListValue<DivorceDocument> doc2 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .documentDateAdded(NOW)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .documents(CaseDocuments.builder().documentsGenerated(asList(doc1, doc2)).build())
            .build();

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        //When
        aosOverduePrinter.sendLetterToApplicant(caseData, TEST_CASE_ID);

        //Then
        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo("aos-overdue");
        assertThat(print.getLetters().size()).isEqualTo(1);
        assertThat(print.getLetters().get(0).getDivorceDocument()).isSameAs(doc1.getValue());
    }

    @Test
    void shouldNotPrintAosOverdueLetterIfRequiredDocumentsAreNotPresent() {
        //Given
        final ListValue<DivorceDocument> doc1 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(RESPONDENT_ANSWERS)
                .documentDateAdded(NOW)
                .build())
            .build();

        final ListValue<DivorceDocument> doc2 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(NAME_CHANGE_EVIDENCE)
                .documentDateAdded(NOW)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .documents(CaseDocuments.builder().documentsGenerated(asList(doc1, doc2)).build())
            .build();

        //When
        aosOverduePrinter.sendLetterToApplicant(caseData, TEST_CASE_ID);

        //Then
        verifyNoInteractions(bulkPrintService);
    }
}
