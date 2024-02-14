package uk.gov.hmcts.sptribs.systemupdate.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.task.CaseTask;

@Component
public class CaseDetailsUpdater {

    private final ObjectMapper objectMapper;

    @Autowired
    public CaseDetailsUpdater(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public CaseDetails<CaseData, State> updateCaseData(final CaseTask caseTask,
                                                       final StartEventResponse startEventResponse) {

        final uk.gov.hmcts.reform.ccd.client.model.CaseDetails initCaseDetails = startEventResponse.getCaseDetails();
        final CaseDetails<CaseData, State> caseDetails = objectMapper
            .convertValue(initCaseDetails, new TypeReference<>() {
            });

        return caseTask.apply(caseDetails);
    }
}
