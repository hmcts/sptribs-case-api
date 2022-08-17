package uk.gov.hmcts.sptribs.common.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.task.CaseTask;
import uk.gov.hmcts.sptribs.common.service.HoldingPeriodService;

import java.time.Clock;
import java.time.LocalDate;

import static java.time.LocalDateTime.now;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Holding;

@Component
@Slf4j
public class SetSubmissionAndDueDate implements CaseTask {

    @Autowired
    private HoldingPeriodService holdingPeriodService;

    @Autowired
    private Clock clock;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final CaseData caseData = caseDetails.getData();

        if (caseDetails.getState().equals(Holding)) {
            final LocalDate issueDate = caseData.getApplication().getIssueDate();
            caseData.setDueDate(holdingPeriodService.getDueDateFor(issueDate));

            log.info("Setting dueDate of {}, for CaseId: {}, State: {}",
                caseData.getDueDate(),
                caseDetails.getId(),
                caseDetails.getState());
        }

        caseData.getAcknowledgementOfService().setDateAosSubmitted(now(clock));

        log.info("Setting dateAosSubmitted of {}, for CaseId: {}, State: {}",
            caseData.getAcknowledgementOfService().getDateAosSubmitted(),
            caseDetails.getId(),
            caseDetails.getState());

        return caseDetails;
    }
}
