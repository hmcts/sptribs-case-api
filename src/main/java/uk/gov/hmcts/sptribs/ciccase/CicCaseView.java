package uk.gov.hmcts.sptribs.ciccase;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CaseView;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;

@Component
public class CicCaseView implements CaseView<CaseData> {
    @Override
    public CaseData getCase(long caseRef, String state, CaseData blobCase) {
        // Invoked whenever CCD needs to load a case.
        // Load up any additional data or perform transformations as needed.
        return blobCase;
    }
}
