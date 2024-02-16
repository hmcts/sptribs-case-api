package uk.gov.hmcts.sptribs.systemupdate.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemMigrateCase.SYSTEM_MIGRATE_CASE;

@ExtendWith(MockitoExtension.class)
class CcdCaseDataContentProviderTest {

    private static final String START_EVENT_TOKEN = "startEventToken";
    private static final String SUMMARY = "Summary";
    private static final String DESCRIPTION = "Description";

    @InjectMocks
    private CcdCaseDataContentProvider ccdCaseDataContentProvider;

    @Mock
    private Object data;

    @Test
    void shouldCreateCaseDataContent() {

        final CaseDataContent caseDataContent = ccdCaseDataContentProvider.createCaseDataContent(
            getStartEventResponse(),
            SUMMARY,
            DESCRIPTION,
            data);

        assertThat(caseDataContent)
            .extracting(
                CaseDataContent::getEventToken,
                CaseDataContent::getData,
                c -> c.getEvent().getId(),
                c -> c.getEvent().getSummary(),
                c -> c.getEvent().getDescription())
            .contains(START_EVENT_TOKEN, data, SYSTEM_MIGRATE_CASE, SUMMARY, DESCRIPTION);
    }

    private StartEventResponse getStartEventResponse() {
        return StartEventResponse.builder()
            .eventId(SYSTEM_MIGRATE_CASE)
            .token(START_EVENT_TOKEN)
            .build();
    }
}
