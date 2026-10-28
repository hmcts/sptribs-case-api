package uk.gov.hmcts.sptribs.systemupdate.schedule.migration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.common.repositories.CaseEventRepository;
import uk.gov.hmcts.sptribs.idam.CICUser;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdUpdateService;

import java.util.List;

import static uk.gov.hmcts.sptribs.systemupdate.event.SystemMigrateFirstHearingDateCase.SYSTEM_MIGRATE_FIRST_HEARING_DATE;

@Component
@Slf4j
@RequiredArgsConstructor
public class SystemMigrateFirstHearingDateTask implements Runnable {

    private final CcdUpdateService ccdUpdateService;
    private final IdamService idamService;
    private final AuthTokenGenerator authTokenGenerator;
    private final CaseEventRepository caseEventRepository;

    @Value("${feature.fix-first-hearing-date-task.caseReference}")
    private String fixFirstHearingDateTestCaseReference;

    @Value("${feature.fix-first-hearing-date-task.enabled}")
    private boolean fixFirstHearingDateEnabled;

    @Override
    public void run() {

        if (!fixFirstHearingDateEnabled) {
            log.info("Fix first hearing date task is not enabled");
            return;
        }

        final CICUser user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        try {

            final List<Long> caseIdsToUpdate;

            if (fixFirstHearingDateTestCaseReference != null && !fixFirstHearingDateTestCaseReference.isEmpty()) {
                Long caseIdToUpdate = Long.valueOf(fixFirstHearingDateTestCaseReference);
                caseIdsToUpdate = List.of(caseIdToUpdate);
            } else {
                caseIdsToUpdate = caseEventRepository.getListOfCasesWithFirstHearingDateAsString();
            }

            if (caseIdsToUpdate.isEmpty()) {
                log.info("Nothing to update");
                return;
            }

            log.info("Cases: {}", caseIdsToUpdate.size());
            for (final Long caseId : caseIdsToUpdate) {
                triggerSystemFixFirstHearingDate(user, serviceAuth, caseId);
            }

            log.info("System fix first hearing date scheduled task complete.");
        } catch (final RuntimeException e) {
            log.error("System fix first hearing date scheduled task stopped after search error", e);
        }
    }

    private void triggerSystemFixFirstHearingDate(CICUser user, String serviceAuth, Long caseId) {
        try {
            log.info("System fix first hearing date Event for Case {}", caseId);

            ccdUpdateService.submitEvent(caseId, SYSTEM_MIGRATE_FIRST_HEARING_DATE, user, serviceAuth);

        } catch (final CcdManagementException e) {
            log.error("Submit event failed for case id: {}, continuing to next case", caseId);
        } catch (final IllegalArgumentException e) {
            log.error("Deserialization failed for case id: {}, continuing to next case", caseId);
        }
    }
}
