package uk.gov.hmcts.sptribs.systemupdate.schedule.cleandata;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.common.repositories.impl.CaseEventRepositoryImpl;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdUpdateService;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.sptribs.systemupdate.event.SystemCleanDeletedDocumentsCase.SYSTEM_CLEAN_DELETED_DOCUMENTS;

@Component
@Slf4j
public class SystemCleanDeletedDocumentsTask implements Runnable {


    private static final String CASE_EVENT_ID = "caseworker-remove-document";
    private static final LocalDate DELETE_FROM_DATE = LocalDate.of(2025,10,1);

    private final CcdUpdateService ccdUpdateService;

    private final IdamService idamService;

    private final AuthTokenGenerator authTokenGenerator;

    private final CaseEventRepositoryImpl caseEventRepositoryImpl;

    @Value("${feature.clean-deleted-documents-task.caseReference}")
    private String cleanDeletedDocumentsTestCaseReference;

    @Value("${feature.clean-deleted-documents-task.enabled}")
    private boolean cleanDeletedDocumentsEnabled;

    @Autowired
    public SystemCleanDeletedDocumentsTask(CcdUpdateService ccdUpdateService,
                                                IdamService idamService,
                                                AuthTokenGenerator authTokenGenerator,
                                           CaseEventRepositoryImpl caseEventRepositoryImpl) {
        this.ccdUpdateService = ccdUpdateService;
        this.idamService = idamService;
        this.authTokenGenerator = authTokenGenerator;
        this.caseEventRepositoryImpl = caseEventRepositoryImpl;
    }

    @Override
    public void run() {

        if (!cleanDeletedDocumentsEnabled) {
            log.info("Clean deleted documents task is not enabled");
            return;
        }

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        try {

            final List<Long> caseIdsToUpdate;

            if (cleanDeletedDocumentsTestCaseReference != null && !cleanDeletedDocumentsTestCaseReference.isEmpty()) {
                Long caseIdToUpdate = Long.valueOf(cleanDeletedDocumentsTestCaseReference);
                caseIdsToUpdate = List.of(caseIdToUpdate);
            } else {
                caseIdsToUpdate = caseEventRepositoryImpl.getListOfCasesByEventTypeAndDate(CASE_EVENT_ID, DELETE_FROM_DATE);
            }

            if (caseIdsToUpdate.isEmpty()) {
                log.info("Nothing to update");
                return;
            }

            log.info("Cases:{}", caseIdsToUpdate.size());
            for (final Long caseId : caseIdsToUpdate) {
                triggerSystemCleanDeletedDocuments(user, serviceAuth, caseId);
            }

            log.info("System clean deleted documents scheduled task complete.");
        } catch (final RuntimeException e) {
            log.error("System clean deleted documents scheduled task stopped after search error", e);
        }

    }

    private void triggerSystemCleanDeletedDocuments(User user, String serviceAuth, Long caseId) {
        try {
            log.info("System clean deleted documents Event for Case {}", caseId);

            ccdUpdateService.submitEvent(caseId, SYSTEM_CLEAN_DELETED_DOCUMENTS, user, serviceAuth);

        } catch (final CcdManagementException e) {
            log.error("Submit event failed for case id: {}, continuing to next case", caseId);
        } catch (final IllegalArgumentException e) {
            log.error("Deserialization failed for case id: {}, continuing to next case", caseId);
        }
    }
}
