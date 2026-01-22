package uk.gov.hmcts.sptribs.caseworker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.common.repositories.StatementRepository;
import uk.gov.hmcts.sptribs.document.model.AcknowledgementCICDocument;
import uk.gov.hmcts.sptribs.notification.persistence.StatementsEntity;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
@Slf4j
public class StatementService {

    private final StatementRepository statementRepository;

    @Autowired
    public StatementService(StatementRepository statementRepository) {
        this.statementRepository = statementRepository;
    }

    public void uploadPartyStatement(AcknowledgementCICDocument uploadedDocument,
                                     String party, String hyphenatedCaseRef) {

        if (uploadedDocument != null) {
            insertToStatement(uploadedDocument.getDocumentLink(), party, hyphenatedCaseRef);
        }

    }

    private void insertToStatement(Document statementDoc, String party, String hyphenatedCaseRef) {

        try {
            StatementsEntity statement = StatementsEntity.builder()
                .caseReferenceNumber(Long.parseLong(hyphenatedCaseRef.replace("-", "")))
                .partyType(party)
                .createdOn(OffsetDateTime.now(ZoneOffset.UTC))
                .documentUrl(statementDoc.getUrl())
                .documentFilename(statementDoc.getFilename())
                .documentBinaryUrl(statementDoc.getBinaryUrl())
                .build();
           statementRepository.save(statement);
        } catch (RestClientException e) {
            log.error("Failed to store pdf document", e);
            throw e;
        }

    }




}
