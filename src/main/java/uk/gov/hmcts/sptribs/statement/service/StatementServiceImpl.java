package uk.gov.hmcts.sptribs.statement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.repositories.StatementRepository;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.statement.model.Statement;
import uk.gov.hmcts.sptribs.statement.model.StatementUpload;
import uk.gov.hmcts.sptribs.statement.persistence.StatementEntity;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static uk.gov.hmcts.sptribs.document.DocumentConstants.DOCUMENT_VALIDATION_MESSAGE;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.isValidDocument;

@Service
@Slf4j
@RequiredArgsConstructor
public class StatementServiceImpl implements StatementService {

    private static final String APPLICANT = "Applicant";
    private static final String RESPONDENT = "Respondent";
    private static final String REPRESENTATIVE = "Representative";
    private static final String INVALID_PARTY_SELECTION = "Please select a valid party for the statement";
    private static final String DOCUMENT_IS_MANDATORY = "Please attach the statement document";
    private static final String VALID_DOCUMENT_EXTENSIONS =
        "pdf,csv,txt,rtf,xlsx,docx,doc,xls,mp3,m4a,mp4,jpg,jpeg,bmp,tif,tiff,png";
    private static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("d MMM yyyy HH:mm");

    private final StatementRepository statementRepository;

    @Override
    public void saveStatement(Long caseReferenceNumber, CaseData caseData) {
        StatementUpload statementUpload = caseData.getStatementUpload();
        if (statementUpload == null
            || statementUpload.getStatementParty() == null
            || statementUpload.getStatementParty().getValue() == null
            || StringUtils.isBlank(statementUpload.getStatementParty().getValue().getLabel())) {
            throw new StatementValidationException(INVALID_PARTY_SELECTION);
        }

        String selectedParty = statementUpload.getStatementParty().getValue().getLabel();
        if (!isValidSelectedParty(selectedParty, caseData)) {
            throw new StatementValidationException(INVALID_PARTY_SELECTION);
        }

        Document statementDocument = statementUpload.getStatementDocument();
        if (statementDocument == null || StringUtils.isBlank(statementDocument.getFilename())) {
            throw new StatementValidationException(DOCUMENT_IS_MANDATORY);
        }

        if (!isValidDocument(statementDocument.getFilename(), VALID_DOCUMENT_EXTENSIONS)) {
            throw new StatementValidationException(DOCUMENT_VALIDATION_MESSAGE);
        }

        try {
            statementRepository.save(
                StatementEntity.builder()
                    .caseReferenceNumber(caseReferenceNumber)
                    .party(selectedParty)
                    .uploadedOn(OffsetDateTime.now(ZoneId.systemDefault()))
                    .documentUrl(statementDocument.getUrl())
                    .documentFilename(statementDocument.getFilename())
                    .documentBinaryUrl(statementDocument.getBinaryUrl())
                    .build()
            );
        } catch (DataAccessException dataAccessException) {
            log.error("Failed to persist statement for case {}", caseReferenceNumber, dataAccessException);
            throw new StatementPersistenceException("Failed to save statement", dataAccessException);
        } catch (RuntimeException runtimeException) {
            log.error("Unexpected error while saving statement for case {}", caseReferenceNumber, runtimeException);
            throw new StatementPersistenceException("Failed to save statement", runtimeException);
        }
    }

    @Override
    public List<ListValue<Statement>> getStatementsForCase(Long caseReferenceNumber) {
        List<ListValue<Statement>> statements = new ArrayList<>();
        AtomicInteger statementListValueIndex = new AtomicInteger(0);

        try {
            for (StatementEntity statementEntity
                : statementRepository.findAllByCaseReferenceNumberOrderByUploadedOnDesc(caseReferenceNumber)) {
                Document statementDocument = Document.builder()
                    .url(statementEntity.getDocumentUrl())
                    .filename(statementEntity.getDocumentFilename())
                    .binaryUrl(statementEntity.getDocumentBinaryUrl())
                    .categoryId(DocumentType.WITNESS_STATEMENT.getCategory())
                    .build();

                Statement statement = Statement.builder()
                    .party(statementEntity.getParty())
                    .uploadedOn(statementEntity.getUploadedOn().atZoneSameInstant(ZoneId.systemDefault())
                        .format(DISPLAY_DATE_FORMAT))
                    .document(statementDocument)
                    .build();

                ListValue<Statement> statementListValue = new ListValue<>();
                statementListValue.setId(String.valueOf(statementListValueIndex.incrementAndGet()));
                statementListValue.setValue(statement);
                statements.add(statementListValue);
            }
        } catch (DataAccessException dataAccessException) {
            log.error("Failed to fetch statements for case {}", caseReferenceNumber, dataAccessException);
            throw new StatementPersistenceException("Failed to fetch statements", dataAccessException);
        } catch (RuntimeException runtimeException) {
            log.error("Unexpected error while fetching statements for case {}", caseReferenceNumber, runtimeException);
            throw new StatementPersistenceException("Failed to fetch statements", runtimeException);
        }

        return statements;
    }

    private boolean isValidSelectedParty(String selectedParty, CaseData caseData) {
        if (StringUtils.equals(selectedParty, APPLICANT)) {
            return StringUtils.isNotBlank(caseData.getCicCase().getApplicantFullName());
        }

        if (StringUtils.equals(selectedParty, RESPONDENT)) {
            return StringUtils.isNotBlank(caseData.getCicCase().getRespondentName());
        }

        if (StringUtils.equals(selectedParty, REPRESENTATIVE)) {
            return StringUtils.isNotBlank(caseData.getCicCase().getRepresentativeFullName());
        }

        return false;
    }
}
