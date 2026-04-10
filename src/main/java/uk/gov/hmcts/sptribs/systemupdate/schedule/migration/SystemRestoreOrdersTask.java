package uk.gov.hmcts.sptribs.systemupdate.schedule.migration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.common.repositories.impl.CaseEventRepositoryImpl;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdUpdateService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemRestoreOrders.SYSTEM_RESTORE_ORDERS;

@RequiredArgsConstructor
@Component
@Slf4j
public class SystemRestoreOrdersTask implements Runnable {

    private static final LocalDate START_FROM_DATE = LocalDate.of(2026, 2, 24);

    private static final LocalDate END_TO_DATE = LocalDate.of(2026, 3, 6);

    private final AuthTokenGenerator authTokenGenerator;

    private final CaseEventRepositoryImpl caseEventRepositoryImpl;

    private final CcdUpdateService ccdUpdateService;

    private final IdamService idamService;

    @Value("${feature.restore-orders-task.enabled}")
    private boolean restoreOrdersTaskEnabled;

    @Value("${feature.restore-orders-task.caseReference}")
    private String restoreOrdersTestCaseReference;

    @Override
    public void run() {
        if (restoreOrdersTaskEnabled) {
            final User user = idamService.retrieveSystemUpdateUserDetails();
            final String serviceAuth = authTokenGenerator.generate();

            log.info("User name: {}", user.getUserDetails().getEmail());
            log.info("User roles: {}", String.join(",", user.getUserDetails().getRoles()));

            final List<Long> caseIdsToUpdate = new ArrayList<>();
            try {
                if (restoreOrdersTestCaseReference != null && !restoreOrdersTestCaseReference.isEmpty()) {
                    Long caseIdToUpdate = Long.valueOf(restoreOrdersTestCaseReference);
                    caseIdsToUpdate.add(caseIdToUpdate);
                } else {
                    caseIdsToUpdate.addAll(caseEventRepositoryImpl.getListOfCasesByEventIdDuringDateRange(
                        CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE, START_FROM_DATE, END_TO_DATE));
                }
            } catch (final RuntimeException e) {
                log.error("System restore orders task stopped after search error", e);
            }

            if (caseIdsToUpdate.isEmpty()) {
                log.info("Nothing to update");
                return;
            }
            log.info("Cases:{}", caseIdsToUpdate.size());
            for (final Long caseId : caseIdsToUpdate) {
                triggerSystemRestoreOrdersEvent(user, serviceAuth, caseId);
            }

            log.info("System restore orders task complete.");

        }
    }

    private void triggerSystemRestoreOrdersEvent(User user, String serviceAuth, Long caseId) {
        try {
            log.info("Running {} event for Case {}", SYSTEM_RESTORE_ORDERS, caseId);

            ccdUpdateService.submitEvent(caseId, SYSTEM_RESTORE_ORDERS, user, serviceAuth);

        } catch (final CcdManagementException e) {
            log.error("Submit event failed for case id: {}, continuing to next case", caseId);
        } catch (final IllegalArgumentException e) {
            log.error("Deserialization failed for case id: {}, continuing to next case", caseId);
        }
    }
}
