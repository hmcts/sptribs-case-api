package uk.gov.hmcts.sptribs.common.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.task.CaseTask;

import java.time.Clock;

import static java.time.LocalDateTime.now;

@Component
@Slf4j
public class SetSubmissionAndDueDate implements CaseTask {


    @Autowired
    private Clock clock;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final CaseData caseData = caseDetails.getData();

        caseData.getAcknowledgementOfService().setDateAosSubmitted(now(clock));

        log.info("Setting dateAosSubmitted of {}, for CaseId: {}, State: {}",
            caseData.getAcknowledgementOfService().getDateAosSubmitted(),
            caseDetails.getId(),
            caseDetails.getState());

        return caseDetails;
    }
}
