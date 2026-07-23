package uk.gov.hmcts.sptribs.systemupdate.schedule.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.common.repositories.impl.CaseDataRepositoryImpl;
import uk.gov.hmcts.sptribs.idam.CICUser;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdUpdateService;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.sptribs.ciccase.model.State.DSS_Draft;
import static uk.gov.hmcts.sptribs.ciccase.model.State.DSS_Expired;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Draft;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemMigrateCaseDocumentsToDocTable.SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE;

@Component
@Slf4j
public class SystemMigrateCaseDocumentsDocumentTableTask implements Runnable {

    private final AuthTokenGenerator authTokenGenerator;

    private final CcdSearchService ccdSearchService;

    private final CcdUpdateService ccdUpdateService;

    private final CaseDataRepositoryImpl caseDataRepository;

    private final IdamService idamService;

    private int totalSuccess = 0;
    private int totalFailure = 0;
    private List<Long> failedCaseIds = new ArrayList<>();

    @Value("${feature.migrate-to-documents-table-task.enabled}")
    private boolean documentTableMigrationEnabled;

    @Value("${feature.migrate-to-documents-table-task.caseReference}")
    private String documentTableTestCaseReference;

    @Value("${feature.migrate-initial-case-documents-task.batchSize}")
    private int batchSize;

    @Value("${feature.migrate-initial-case-documents-task.batchPauseMs}")
    private long batchPauseMs;

    @Autowired
    public SystemMigrateCaseDocumentsDocumentTableTask(AuthTokenGenerator authTokenGenerator, CcdSearchService ccdSearchService,
                                                       CcdUpdateService ccdUpdateService, CaseDataRepositoryImpl caseDataRepository,
                                                       IdamService idamService) {
        this.authTokenGenerator = authTokenGenerator;
        this.ccdSearchService = ccdSearchService;
        this.ccdUpdateService = ccdUpdateService;
        this.caseDataRepository = caseDataRepository;
        this.idamService = idamService;
    }


    @Override
    public void run() {
        log.info("Starting document table migration");
        Instant start = Instant.now();

        if (!documentTableMigrationEnabled) {
            log.info("Migrate documents to document table task is not enabled");
            return;
        }

        final CICUser user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();


        try {

            final List<Long> caseIdsToUpdate;

            if (documentTableTestCaseReference != null && !documentTableTestCaseReference.isEmpty()) {
                Long caseIdToUpdate = Long.valueOf(documentTableTestCaseReference);
                caseIdsToUpdate = List.of(caseIdToUpdate);
            } else {

                caseIdsToUpdate = caseDataRepository.returnAllCasesExlcudingStates(
                    List.of(DSS_Draft.name(), Draft.name(), DSS_Expired.name()));
            }

            if (caseIdsToUpdate.isEmpty()) {
                log.info("Found no cases");
                return;
            }

            log.info("Cases:{}", caseIdsToUpdate.size());

            processBatches(caseIdsToUpdate, user, serviceAuth);

            Duration elapsedTime = Duration.between(start, Instant.now());

            log.info("System migrate documents to document table scheduled task complete.");
            log.info("Migrated {} cases in {} seconds", totalSuccess, elapsedTime.getSeconds());
        } catch (final RuntimeException e) {
            log.error("System migrate documents to document table scheduled task stopped after search error", e);
        }

    }

    private void triggerSystemMigrateDocumentsToDocumentTable(CICUser user, String serviceAuth, Long caseId) {
        try {
            log.info("System migrate documents to document table Event for Case {}", caseId);

            ccdUpdateService.submitEvent(caseId, SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE, user, serviceAuth);

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
                log.info("Batch {}/{} complete: {} processed, {} failed",
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
                triggerSystemMigrateDocumentsToDocumentTable(user, serviceAuth, caseIds.get(i));
                success++;
            } catch (final Exception e) {
                failed++;
                failedCaseIds.add(caseIds.get(i));
                log.error("Failed to migrate case {}", caseIds.get(i), e);
            }
        }

        totalSuccess = totalSuccess + success;
        totalFailure = totalFailure + failed;

        log.info("Migration complete: {} processed, {} failed out of {} total", success, failed, total);

        if (!failedCaseIds.isEmpty()) {
            log.error("Failed case reference's: {}", failedCaseIds);
        }
    }
}
