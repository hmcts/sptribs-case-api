package uk.gov.hmcts.sptribs.edgecase.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.edgecase.model.CaseData;
import uk.gov.hmcts.sptribs.edgecase.model.State;

import java.time.LocalDate;

@Component
@Slf4j
public class InitialiseSolicitorCreatedApplication implements CaseTask {

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {
        final LocalDate createdDate = caseDetails.getCreatedDate().toLocalDate();
        //caseDetails.getData().getApplication().setCreatedDate(createdDate);

        log.info(
            "Setting application createdDate to {} for CaseId: {}, State: {}",
            createdDate,
            caseDetails.getId(),
            caseDetails.getState()
        );

        return caseDetails;
    }
}
