package uk.gov.hmcts.sptribs.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.caseworker.service.ExtendedCaseDataApi;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.services.model.AuditEvent;
import uk.gov.hmcts.sptribs.services.model.AuditEventsResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditEventServiceTest {

    private static final User SYSTEM_USER = new User("sys-token", null);

    private static final String SERVICE_TOKEN = "service-token";

    @Mock
    private ExtendedCaseDataApi extendedCaseDataApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private IdamService idamService;

    @InjectMocks
    private AuditEventService auditEventService;

    @Test
    void shouldReturnFalseWhenApiResponseIsNull() {
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(SYSTEM_USER);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(extendedCaseDataApi.getAuditEvents(any(), anyString(), anyBoolean(), anyString()))
            .thenReturn(null);

        boolean result = auditEventService.hasCaseEvent("case-1", "event-1");

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenNoMatchingEvent() {
        List<AuditEvent> events = List.of(AuditEvent.builder().id("other-event").build());
        AuditEventsResponse response = AuditEventsResponse.builder().auditEvents(events).build();
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(SYSTEM_USER);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);

        when(extendedCaseDataApi.getAuditEvents(any(), anyString(), anyBoolean(), anyString()))
            .thenReturn(response);

        boolean result = auditEventService.hasCaseEvent("case-1", "event-1");

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnTrueWhenMatchingEventExists() {
        List<AuditEvent> events = List.of(AuditEvent.builder().id("event-1").build(), AuditEvent.builder().id("other").build());
        AuditEventsResponse response = AuditEventsResponse.builder().auditEvents(events).build();

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(SYSTEM_USER);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);

        when(extendedCaseDataApi.getAuditEvents(any(), anyString(), anyBoolean(), anyString()))
            .thenReturn(response);

        boolean result = auditEventService.hasCaseEvent("case-1", "event-1");

        assertThat(result).isTrue();
    }
}
