package uk.gov.hmcts.sptribs.ccd;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;

@Component
public class CaseRepository implements uk.gov.hmcts.ccd.sdk.CaseRepository<CaseData> {
    @Override
    public CaseData getCase(long caseRef, String state, CaseData data) {
        return data;
    }
}
