package uk.gov.hmcts.sptribs.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.caseworker.service.ExtendedCaseDataApi;
import uk.gov.hmcts.sptribs.idam.IdamService;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuditEventService {

    private final ExtendedCaseDataApi extendedCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final IdamService idamService;


    public boolean hasCaseEvent(String caseId, String eventId) {
        var authToken = idamService.retrieveSystemUpdateUserDetails().getAuthToken();
        var serviceAuthToken = authTokenGenerator.generate();

        var response = extendedCaseDataApi.getAuditEvents(
            authToken,
            serviceAuthToken,
            caseId
        );

        if (eventId == null || response == null || response.getAuditEvents() == null) {
            log.warn("No audit events found for caseId: {}", caseId);
            return false;
        }

        return response.getAuditEvents()
            .stream()
            .anyMatch(auditEvent -> eventId.equals(auditEvent.getId()));
    }
}
