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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.RESPONDENT_DOCUMENT_MANAGEMENT;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemMigrateInitialCaseDocuments.SYSTEM_MIGRATE_INITIAL_CASE_DOCUMENTS;

@RequiredArgsConstructor
@Component
@Slf4j
public class SystemMigrateInitialCaseDocumentsTask implements Runnable {
    private final AuthTokenGenerator authTokenGenerator;
    private final CaseEventRepository caseEventRepository;
    private final CcdUpdateService ccdUpdateService;
    private final IdamService idamService;

    @Value("${feature.migrate-initial-case-documents-task.enabled}")
    private boolean migrateInitialDocsEnabled;

    @Value("${feature.migrate-initial-case-documents-task.caseReference}")
    private String migrateInitialDocsCaseRef;

    @Value("${feature.migrate-initial-case-documents-task.skipMigrated}")
    private boolean skipMigrated;

    @Value("${feature.migrate-initial-case-documents-task.batchSize}")
    private int batchSize;

    @Value("${migration.migrate-initial-case-documents-task.batchPauseMs:5000}")
    private long batchPauseMs;

    @Override
    public void run() {
        if (migrateInitialDocsEnabled) {
            final CICUser user = idamService.retrieveSystemUpdateUserDetails();
            final String serviceAuth = authTokenGenerator.generate();

            log.info("User name: {}", user.getUserInfo().getSub());
            log.info("User roles: {}", String.join(",", user.getUserInfo().getRoles()));

            final List<Long> caseIdsToUpdate = new ArrayList<>();
            try {
                if (migrateInitialDocsCaseRef != null && !migrateInitialDocsCaseRef.isEmpty()) {
                    List<Long> casesToUpdate = Arrays.stream(
                        migrateInitialDocsCaseRef.split(",")).map(s -> Long.parseLong(s.trim())).toList();
                    caseIdsToUpdate.addAll(casesToUpdate);
                    log.info("Manual case list provided: {} cases", caseIdsToUpdate.size());
                } else if (skipMigrated) {
                    caseIdsToUpdate.addAll(caseEventRepository.getListOfCasesByEventIdDuringDateRange(
                        RESPONDENT_DOCUMENT_MANAGEMENT,
                        LocalDate.of(2025, 10, 16),
                        LocalDate.now(),
                        SYSTEM_MIGRATE_INITIAL_CASE_DOCUMENTS));
                    log.info("Auto-discovery (skip migrated): {} cases", caseIdsToUpdate.size());
                } else {
                    caseIdsToUpdate.addAll(caseEventRepository.getListOfCasesByEventIdDuringDateRange(
                        RESPONDENT_DOCUMENT_MANAGEMENT,
                        LocalDate.of(2025, 10, 16),
                        LocalDate.now()));
                }
            } catch (final RuntimeException e) {
                log.error("System migrate initial case documents task stopped after search error", e);
                return;
            }

            if (caseIdsToUpdate.isEmpty()) {
                log.info("No cases to update");
                return;
            }

            processBatches(caseIdsToUpdate, user, serviceAuth);
        }
    }

    private void triggerSystemMigrateInitialCaseDocuments(CICUser user, String serviceAuth, Long caseId) {
        try {
            log.info("Running {} event for Case {}", SYSTEM_MIGRATE_INITIAL_CASE_DOCUMENTS, caseId);

            ccdUpdateService.submitEvent(caseId, SYSTEM_MIGRATE_INITIAL_CASE_DOCUMENTS, user, serviceAuth);

        } catch (final CcdManagementException e) {
            log.error("Submit event failed for case id: {}, continuing to next case", caseId);
        } catch (final IllegalArgumentException e) {
            log.error("Deserialization failed for case id: {}, continuing to next case", caseId);
        }
    }

    private void processBatches(List<Long> caseIds, CICUser user, String serviceAuth) {
        int total = caseIds.size();
        int success = 0;
        int failed = 0;
        int batchCount = (total + batchSize - 1) / batchSize;

        log.info("Processing {} cases in {} batches of {}", total, batchCount, batchSize);

        for (int i = 0; i < total; i++) {
            if (i > 0 && i % batchSize == 0) {
                log.info("Batch {}/{} complete: {} succeeded, {} failed so far",
                        i / batchSize, batchCount, success, failed);
                try {
                    Thread.sleep(batchPauseMs);
                } catch (InterruptedException e) {
                    log.warn("Batch processing interrupted, stopping migration");
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            try {
                triggerSystemMigrateInitialCaseDocuments(user, serviceAuth, caseIds.get(i));
                success++;
            } catch (final Exception e) {
                failed++;
                log.error("Failed to migrate case {}", caseIds.get(i), e);
            }
        }

        log.info("Migration complete: {} succeeded, {} failed out of {} total", success, failed, total);
    }
}
