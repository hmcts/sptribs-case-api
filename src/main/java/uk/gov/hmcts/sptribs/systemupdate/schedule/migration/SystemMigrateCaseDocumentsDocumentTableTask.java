package uk.gov.hmcts.sptribs.systemupdate.schedule.migration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdUpdateService;

import java.util.List;

import static reactor.netty.http.HttpConnectionLiveness.log;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemMigrateCaseDocumentsToDocTable.SYSTEM_MIGRATE_CASE_DOCUMENTS_TO_TABLE;

public class SystemMigrateCaseDocumentsDocumentTableTask implements Runnable {

    private final AuthTokenGenerator authTokenGenerator;

    private final CcdSearchService ccdSearchService;

    private final CcdUpdateService ccdUpdateService;

    private final IdamService idamService;

    @Value("${feature.migrate-documents-table-task.enabled}")
    private boolean documentTableMigrationEnabled;

    @Value("${feature.migrate-global-search-task.caseReference}")
    private String documentTableTestCaseReference;

    @Autowired
    public SystemMigrateCaseDocumentsDocumentTableTask(AuthTokenGenerator authTokenGenerator, CcdSearchService ccdSearchService,
                                         CcdUpdateService ccdUpdateService, IdamService idamService) {
        this.authTokenGenerator = authTokenGenerator;
        this.ccdSearchService = ccdSearchService;
        this.ccdUpdateService = ccdUpdateService;
        this.idamService = idamService;
    }


    @Override
    public void run() {

        if (!documentTableMigrationEnabled) {
            log.info("Clean deleted documents task is not enabled");
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

                caseIdsToUpdate = List.of(123L);

                //get all case references from case_data

            }

            if (caseIdsToUpdate.isEmpty()) {
                log.info("Nothing to update");
                return;
            }

            log.info("Cases:{}", caseIdsToUpdate.size());
            for (final Long caseId : caseIdsToUpdate) {

                triggerSystemMigrateDocumentsToDocumentTable(user, serviceAuth, caseId);
            }

            log.info("System clean deleted documents scheduled task complete.");
        } catch (final RuntimeException e) {
            log.error("System clean deleted documents scheduled task stopped after search error", e);
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
}
