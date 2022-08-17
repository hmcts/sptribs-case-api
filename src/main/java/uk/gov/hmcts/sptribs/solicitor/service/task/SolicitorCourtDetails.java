package uk.gov.hmcts.sptribs.solicitor.service.task;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.task.CaseTask;

import static uk.gov.hmcts.sptribs.ciccase.model.Court.SERVICE_CENTRE;

@Component
public class SolicitorCourtDetails implements CaseTask {

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final CaseData caseData = caseDetails.getData();

        caseData.setDivorceUnit(SERVICE_CENTRE);

        return caseDetails;
    }
}
