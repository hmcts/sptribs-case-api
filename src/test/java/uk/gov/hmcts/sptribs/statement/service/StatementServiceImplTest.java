package uk.gov.hmcts.sptribs.statement.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.repositories.StatementRepository;
import uk.gov.hmcts.sptribs.statement.model.StatementUpload;
import uk.gov.hmcts.sptribs.statement.persistence.StatementEntity;

import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatementServiceImplTest {

    @InjectMocks
    private StatementServiceImpl statementService;

    @Mock
    private StatementRepository statementRepository;

    @Test
    void shouldSaveStatementWhenDataIsValid() {
        CaseData caseData = caseDataWithUpload("Applicant", "statement.pdf");

        statementService.saveStatement(1234567890123456L, caseData);

        ArgumentCaptor<StatementEntity> statementEntityCaptor = ArgumentCaptor.forClass(StatementEntity.class);
        verify(statementRepository).save(statementEntityCaptor.capture());
        assertThat(statementEntityCaptor.getValue().getId()).isNull();
    }

    @Test
    void shouldThrowValidationExceptionWhenStatementUploadIsNull() {
        CaseData caseData = CaseData.builder().cicCase(CicCase.builder().build()).build();

        assertThatThrownBy(() -> statementService.saveStatement(1234567890123456L, caseData))
            .isInstanceOf(StatementValidationException.class)
            .hasMessage("Please select a valid party for the statement");
    }

    @Test
    void shouldThrowValidationExceptionWhenSelectedPartyIsMissing() {
        StatementUpload upload = StatementUpload.builder()
            .statementDocument(Document.builder()
                .url("http://dm/documents/abc")
                .binaryUrl("http://dm/documents/abc/binary")
                .filename("statement.pdf")
                .build())
            .build();
        CaseData caseData = CaseData.builder()
            .statementUpload(upload)
            .cicCase(CicCase.builder().build())
            .build();

        assertThatThrownBy(() -> statementService.saveStatement(1234567890123456L, caseData))
            .isInstanceOf(StatementValidationException.class)
            .hasMessage("Please select a valid party for the statement");
    }

    @Test
    void shouldThrowValidationExceptionForInvalidParty() {
        CaseData caseData = caseDataWithUpload("Representative", "statement.pdf");
        caseData.getCicCase().setRepresentativeFullName("");

        assertThatThrownBy(() -> statementService.saveStatement(1234567890123456L, caseData))
            .isInstanceOf(StatementValidationException.class)
            .hasMessage("Please select a valid party for the statement");
    }

    @Test
    void shouldThrowValidationExceptionForInvalidDocumentType() {
        CaseData caseData = caseDataWithUpload("Applicant", "statement.exe");

        assertThatThrownBy(() -> statementService.saveStatement(1234567890123456L, caseData))
            .isInstanceOf(StatementValidationException.class)
            .hasMessage("Please upload valid document");
    }

    @Test
    void shouldThrowValidationExceptionForMissingDocument() {
        CaseData caseData = caseDataWithUpload("Applicant", "statement.pdf");
        caseData.getStatementUpload().setStatementDocument(null);

        assertThatThrownBy(() -> statementService.saveStatement(1234567890123456L, caseData))
            .isInstanceOf(StatementValidationException.class)
            .hasMessage("Please attach the statement document");
    }

    @Test
    void shouldThrowValidationExceptionForBlankDocumentFilename() {
        CaseData caseData = caseDataWithUpload("Applicant", "statement.pdf");
        caseData.getStatementUpload().setStatementDocument(Document.builder()
            .url("http://dm/documents/abc")
            .binaryUrl("http://dm/documents/abc/binary")
            .filename(" ")
            .build());

        assertThatThrownBy(() -> statementService.saveStatement(1234567890123456L, caseData))
            .isInstanceOf(StatementValidationException.class)
            .hasMessage("Please attach the statement document");
    }

    @Test
    void shouldThrowPersistenceExceptionWhenRepositoryFailsOnSave() {
        CaseData caseData = caseDataWithUpload("Applicant", "statement.pdf");
        when(statementRepository.save(any(StatementEntity.class)))
            .thenThrow(new DataAccessResourceFailureException("db down"));

        assertThatThrownBy(() -> statementService.saveStatement(1234567890123456L, caseData))
            .isInstanceOf(StatementPersistenceException.class)
            .hasMessage("Failed to save statement");
    }

    @Test
    void shouldThrowPersistenceExceptionWhenRepositoryThrowsRuntimeExceptionOnSave() {
        CaseData caseData = caseDataWithUpload("Applicant", "statement.pdf");
        when(statementRepository.save(any(StatementEntity.class))).thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> statementService.saveStatement(1234567890123456L, caseData))
            .isInstanceOf(StatementPersistenceException.class)
            .hasMessage("Failed to save statement")
            .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldMapStatementsFromRepository() {
        when(statementRepository.findAllByCaseReferenceNumberOrderByUploadedOnDesc(1234567890123456L))
            .thenReturn(List.of(StatementEntity.builder()
                .id(BigInteger.ONE)
                .caseReferenceNumber(1234567890123456L)
                .party("Applicant")
                .uploadedOn(OffsetDateTime.now())
                .documentUrl("http://dm/documents/abc")
                .documentFilename("statement.pdf")
                .documentBinaryUrl("http://dm/documents/abc/binary")
                .build()));

        var statements = statementService.getStatementsForCase(1234567890123456L);

        assertThat(statements).hasSize(1);
        assertThat(statements.getFirst().getValue().getParty()).isEqualTo("Applicant");
        assertThat(statements.getFirst().getValue().getDocument().getFilename()).isEqualTo("statement.pdf");
    }

    @Test
    void shouldUseSequentialListValueIdsWhenMappingStatementsFromRepository() {
        when(statementRepository.findAllByCaseReferenceNumberOrderByUploadedOnDesc(1234567890123456L))
            .thenReturn(List.of(
                StatementEntity.builder()
                    .id(BigInteger.valueOf(22L))
                    .caseReferenceNumber(1234567890123456L)
                    .party("Applicant")
                    .uploadedOn(OffsetDateTime.now())
                    .documentUrl("http://dm/documents/first")
                    .documentFilename("first.pdf")
                    .documentBinaryUrl("http://dm/documents/first/binary")
                    .build(),
                StatementEntity.builder()
                    .id(BigInteger.valueOf(77L))
                    .caseReferenceNumber(1234567890123456L)
                    .party("Respondent")
                    .uploadedOn(OffsetDateTime.now().minusMinutes(1))
                    .documentUrl("http://dm/documents/second")
                    .documentFilename("second.pdf")
                    .documentBinaryUrl("http://dm/documents/second/binary")
                    .build()
            ));

        var statements = statementService.getStatementsForCase(1234567890123456L);

        assertThat(statements).hasSize(2);
        assertThat(statements.get(0).getId()).isEqualTo("1");
        assertThat(statements.get(1).getId()).isEqualTo("2");
    }

    @Test
    void shouldThrowPersistenceExceptionWhenRepositoryFailsOnRead() {
        when(statementRepository.findAllByCaseReferenceNumberOrderByUploadedOnDesc(1234567890123456L))
            .thenThrow(new DataAccessResourceFailureException("db down"));

        assertThatThrownBy(() -> statementService.getStatementsForCase(1234567890123456L))
            .isInstanceOf(StatementPersistenceException.class)
            .hasMessage("Failed to fetch statements");
    }

    @Test
    void shouldThrowPersistenceExceptionWhenRepositoryThrowsRuntimeExceptionOnRead() {
        when(statementRepository.findAllByCaseReferenceNumberOrderByUploadedOnDesc(eq(1234567890123456L)))
            .thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> statementService.getStatementsForCase(1234567890123456L))
            .isInstanceOf(StatementPersistenceException.class)
            .hasMessage("Failed to fetch statements")
            .hasCauseInstanceOf(RuntimeException.class);
    }

    private CaseData caseDataWithUpload(String selectedParty, String filename) {
        DynamicListElement selectedElement = DynamicListElement.builder()
            .label(selectedParty)
            .code(UUID.randomUUID())
            .build();

        StatementUpload upload = StatementUpload.builder()
            .statementParty(DynamicList.builder().value(selectedElement).listItems(List.of(selectedElement)).build())
            .statementDocument(Document.builder()
                .url("http://dm/documents/abc")
                .binaryUrl("http://dm/documents/abc/binary")
                .filename(filename)
                .build())
            .build();

        CaseData caseData = CaseData.builder().build();
        caseData.setStatementUpload(upload);
        caseData.setCicCase(CicCase.builder()
            .applicantFullName("Applicant Person")
            .respondentName("Appeals team")
            .representativeFullName("Representative Person")
            .build());
        return caseData;
    }
}
