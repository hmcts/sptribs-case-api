package uk.gov.hmcts.sptribs.systemupdate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.sptribs.ciccase.task.CaseTask;

@Component
public class CcdCaseDataContentProvider {

    private final CaseDetailsUpdater caseDetailsUpdater;

    @Autowired
    public CcdCaseDataContentProvider(CaseDetailsUpdater caseDetailsUpdater) {
        this.caseDetailsUpdater = caseDetailsUpdater;
    }

    public CaseDataContent createCaseDataContent(final StartEventResponse startEventResponse,
                                                 final String summary,
                                                 final String description) {

        return createCaseDataContent(
            startEventResponse,
            summary,
            description,
            startEventResponse.getCaseDetails().getData()
        );
    }

    public CaseDataContent createCaseDataContent(final StartEventResponse startEventResponse,
                                                 final String summary,
                                                 final String description,
                                                 final CaseTask caseTask) {

        return createCaseDataContent(
            startEventResponse,
            summary,
            description,
            caseDetailsUpdater.updateCaseData(caseTask, startEventResponse).getData()
        );
    }

    private CaseDataContent createCaseDataContent(final StartEventResponse startEventResponse,
                                                 final String summary,
                                                 final String description,
                                                 final Object data) {

        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(
                Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary(summary)
                    .description(description)
                    .build())
            .data(data)
            .build();
    }
}
