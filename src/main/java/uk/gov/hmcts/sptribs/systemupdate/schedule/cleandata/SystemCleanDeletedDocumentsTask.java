package uk.gov.hmcts.sptribs.systemupdate.schedule.cleandata;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.systemupdate.repository.CaseEventRepository;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdSearchCaseException;
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

    private final CaseEventRepository caseEventRepository;

    @Autowired
    public SystemCleanDeletedDocumentsTask(CcdUpdateService ccdUpdateService,
                                                IdamService idamService,
                                                AuthTokenGenerator authTokenGenerator,
                                           CaseEventRepository caseEventRepository) {
        this.ccdUpdateService = ccdUpdateService;
        this.idamService = idamService;
        this.authTokenGenerator = authTokenGenerator;
        this.caseEventRepository = caseEventRepository;
    }

    @Override
//    @Scheduled(cron = "0 * * * * *")
    public void run() {
        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        try {

            final List<Long> caseIdsToUpdate = caseEventRepository.getListOfCasesByEventTypeAndDate(CASE_EVENT_ID, DELETE_FROM_DATE);

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
