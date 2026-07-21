package uk.gov.hmcts.sptribs.systemupdate.schedule.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.common.repositories.impl.CaseDataRepositoryImpl;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdUpdateService;

import java.util.List;

import static uk.gov.hmcts.sptribs.systemupdate.event.SystemMigrateCaseDocumentsToDocTable.SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE;

@Component
@Slf4j
public class SystemMigrateCaseDocumentsDocumentTableTask implements Runnable {

    private final AuthTokenGenerator authTokenGenerator;

    private final CcdSearchService ccdSearchService;

    private final CcdUpdateService ccdUpdateService;

    private final CaseDataRepositoryImpl caseDataRepository;

    private final IdamService idamService;

    @Value("${feature.migrate-to-documents-table-task.enabled}")
    private boolean documentTableMigrationEnabled;

    @Value("${feature.migrate-to-documents-table-task.caseReference}")
    private String documentTableTestCaseReference;

    @Value("${feature.migrate-initial-case-documents-task.batchSize:10}")
    private int batchSize;

    @Value("${migration.migrate-initial-case-documents-task.batchPauseMs:5000}")
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

        if (!documentTableMigrationEnabled) {
            log.info("Migrate documents to document table task is not enabled");
            return;
        }

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();


        try {

            final List<Long> caseIdsToUpdate;

            if (documentTableTestCaseReference != null && !documentTableTestCaseReference.isEmpty()) {
                Long caseIdToUpdate = Long.valueOf(documentTableTestCaseReference);
                caseIdsToUpdate = List.of(caseIdToUpdate);
            } else {

                caseIdsToUpdate = caseDataRepository.returnAllCases("Draft");
            }

            if (caseIdsToUpdate.isEmpty()) {
                log.info("Nothing to update");
                return;
            }

            log.info("Cases:{}", caseIdsToUpdate.size());

            processBatches(caseIdsToUpdate, user, serviceAuth);

            log.info("System migrate documents to document table scheduled task complete.");
        } catch (final RuntimeException e) {
            log.error("System migrate documents to document table scheduled task stopped after search error", e);
        }

    }

    private void triggerSystemMigrateDocumentsToDocumentTable(User user, String serviceAuth, Long caseId) {
        try {
            log.info("System migrate documents to document table Event for Case {}", caseId);

            ccdUpdateService.submitEvent(caseId, SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE, user, serviceAuth);

        } catch (final CcdManagementException e) {
            log.error("Submit event failed for case id: {}, continuing to next case", caseId);
        } catch (final IllegalArgumentException e) {
            log.error("Deserialization failed for case id: {}, continuing to next case", caseId);
        }
    }

    private void processBatches(List<Long> caseIds, User user, String serviceAuth) {
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
                triggerSystemMigrateDocumentsToDocumentTable(user, serviceAuth, caseIds.get(i));
                success++;
            } catch (final Exception e) {
                failed++;
                log.error("Failed to migrate case {}", caseIds.get(i), e);
            }
        }

        log.info("Migration complete: {} succeeded, {} failed out of {} total", success, failed, total);
    }
}
