package uk.gov.hmcts.sptribs.ciccase;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CaseView;
import uk.gov.hmcts.ccd.sdk.CaseViewRequest;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;

@Component
public class CicCaseView implements CaseView<CaseData, State> {
    @Override
    public CaseData getCase(CaseViewRequest<State> request, CaseData blobCase) {
        // Invoked whenever CCD needs to load a case.
        // Load up any additional data or perform transformations as needed.
        return blobCase;
    }
}
