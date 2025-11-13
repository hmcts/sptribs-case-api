package uk.gov.hmcts.sptribs.ciccase;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CaseView;
import uk.gov.hmcts.ccd.sdk.CaseViewRequest;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.CriminalInjuriesCompensationData;

@Component
public class CicCaseView implements CaseView<CriminalInjuriesCompensationData, State> {
    @Override
    public CriminalInjuriesCompensationData getCase(CaseViewRequest<State> request,
                                                   CriminalInjuriesCompensationData blobCase) {
        // Invoked whenever CCD needs to load a case.
        // Load up any additional data or perform transformations as needed.
        return blobCase;
    }
}
