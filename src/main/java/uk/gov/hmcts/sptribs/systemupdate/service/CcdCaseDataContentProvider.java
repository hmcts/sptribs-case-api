package uk.gov.hmcts.sptribs.systemupdate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.sptribs.ciccase.task.CaseTask;

import java.util.HashMap;
import java.util.Map;

@Component
public class CcdCaseDataContentProvider {

    private static final String FIRST_HEARING_DATE = "firstHearingDate";

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

    public CaseDataContent createContentClearingFirstHearingDate(final StartEventResponse startEventResponse,
                                                                  final String summary,
                                                                  final String description) {
        // Keep all CCD fields that are no longer modelled by CaseData, and explicitly clear the retired key.
        final Map<String, Object> data = new HashMap<>(startEventResponse.getCaseDetails().getData());
        data.put(FIRST_HEARING_DATE, null);
        return createCaseDataContent(startEventResponse, summary, description, data);
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
