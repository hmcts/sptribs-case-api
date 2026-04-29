package uk.gov.hmcts.sptribs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.sptribs.DAO.Statement;

import java.util.List;
import java.util.UUID;

public interface StatementRepository extends JpaRepository<Statement, UUID> {
    List<Statement> findByCaseReferenceNumber(Long caseReferenceNumber);
}
