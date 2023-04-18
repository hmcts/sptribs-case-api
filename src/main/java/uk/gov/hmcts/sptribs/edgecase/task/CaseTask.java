package uk.gov.hmcts.sptribs.edgecase.task;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.edgecase.model.CaseData;
import uk.gov.hmcts.sptribs.edgecase.model.State;

import java.util.function.Function;

public interface CaseTask extends Function<CaseDetails<CaseData, State>, CaseDetails<CaseData, State>> {
}
