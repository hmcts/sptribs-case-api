package uk.gov.hmcts.sptribs.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.sptribs.DTO.StatementDTO;
import uk.gov.hmcts.sptribs.repository.StatementRepository;
import uk.gov.hmcts.sptribs.DAO.Statement;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatementService {

    private final StatementRepository statementRepository;

    public void saveStatement(StatementDTO statementDTO) {
        statementRepository.save(Statement.builder()
            .caseReferenceNumber(statementDTO.getCaseReferenceNumber())
            .partyType(statementDTO.getPartyType())
            .documentUrl(statementDTO.getDocumentUrl())
            .documentFilename(statementDTO.getDocumentFilename())
            .documentBinaryUrl(statementDTO.getDocumentBinaryUrl())
            .uploadedAt(LocalDateTime.now())
            .build());
    }

    public List<StatementDTO> getStatements(Long caseReferenceNumber) {
        return statementRepository.findByCaseReferenceNumber(caseReferenceNumber)
            .stream()
            .map(dao -> StatementDTO.builder()
                .caseReferenceNumber(dao.getCaseReferenceNumber())
                .partyType(dao.getPartyType())
                .documentUrl(dao.getDocumentUrl())
                .documentFilename(dao.getDocumentFilename())
                .documentBinaryUrl(dao.getDocumentBinaryUrl())
                .build())
            .toList();
    }
}
