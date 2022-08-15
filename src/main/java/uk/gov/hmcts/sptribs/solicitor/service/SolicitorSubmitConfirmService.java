package uk.gov.hmcts.sptribs.solicitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.solicitor.service.task.SetConfirmServiceDueDate;

import static uk.gov.hmcts.sptribs.ciccase.task.CaseTaskRunner.caseTasks;

@Service
@Slf4j
public class SolicitorSubmitConfirmService {
    @Autowired
    private SetConfirmServiceDueDate setConfirmServiceDueDate;

    public CaseDetails<CaseData, State> submitConfirmService(final CaseDetails<CaseData, State> caseDetails) {
        return caseTasks(
            setConfirmServiceDueDate
        ).run(caseDetails);
    }
}
