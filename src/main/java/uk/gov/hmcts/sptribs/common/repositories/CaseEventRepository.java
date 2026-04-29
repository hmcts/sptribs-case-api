package uk.gov.hmcts.sptribs.common.repositories;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;

import java.time.LocalDate;
import java.util.List;

public interface CaseEventRepository {

    List<CaseData> getFirstEventDataForCase(Long reference, String caseEventId);

    List<Long> getListOfCasesByEventTypeAndDate(String caseEventId, LocalDate createdDate);

    List<Long> getListOfCasesByEventIdDuringDateRange(String caseEventId, LocalDate startDate, LocalDate endDate);
}
