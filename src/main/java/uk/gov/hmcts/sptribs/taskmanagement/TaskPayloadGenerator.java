package uk.gov.hmcts.sptribs.taskmanagement;

import uk.gov.hmcts.ccd.sdk.taskmanagement.model.TaskPayload;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;

public interface TaskPayloadGenerator {
    TaskPayload getTaskPayload(CaseData caseData, long caseId);
}




