package uk.gov.hmcts.sptribs.systemupdate.schedule.migration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.common.repositories.CaseEventRepository;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdUpdateService;

import java.util.ArrayList;
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

    @Override
    public void run() {
        if (migrateInitialDocsEnabled) {
            final User user = idamService.retrieveSystemUpdateUserDetails();
            final String serviceAuth = authTokenGenerator.generate();

            log.info("User name: {}", user.getUserDetails().getEmail());
            log.info("User roles: {}", String.join(",", user.getUserDetails().getRoles()));

            final List<Long> caseIdsToUpdate = new ArrayList<>();
            try {
                if (migrateInitialDocsCaseRef != null && !migrateInitialDocsCaseRef.isEmpty()) {
                    Long caseIdToUpdate = Long.valueOf(migrateInitialDocsCaseRef);
                    caseIdsToUpdate.add(caseIdToUpdate);
                } else {
                    caseIdsToUpdate.addAll(caseEventRepository.getCasesWithEvent(RESPONDENT_DOCUMENT_MANAGEMENT));
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
            log.info("Running {} event for Case {}", SYSTEM_MIGRATE_INITIAL_CASE_DOCUMENTS, caseId);

            ccdUpdateService.submitEvent(caseId, SYSTEM_MIGRATE_INITIAL_CASE_DOCUMENTS, user, serviceAuth);

        } catch (final CcdManagementException e) {
            log.error("Submit event failed for case id: {}, continuing to next case", caseId);
        } catch (final IllegalArgumentException e) {
            log.error("Deserialization failed for case id: {}, continuing to next case", caseId);
        }
    }
}
