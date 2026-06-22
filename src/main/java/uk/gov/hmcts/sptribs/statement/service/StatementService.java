package uk.gov.hmcts.sptribs.statement.service;

import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.statement.model.Statement;

import java.util.List;

public interface StatementService {

    void saveStatement(Long caseReferenceNumber, CaseData caseData);

    List<ListValue<Statement>> getStatementsForCase(Long caseReferenceNumber);
}
