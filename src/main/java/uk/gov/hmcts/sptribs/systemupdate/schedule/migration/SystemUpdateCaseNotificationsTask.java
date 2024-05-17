package uk.gov.hmcts.sptribs.systemupdate.schedule.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.model.CaseNotification;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdUpdateService;

import java.util.List;

@Component
@Slf4j
public class SystemUpdateCaseNotificationsTask implements Runnable {
    private final IdamService idamService;

    private final CcdSearchService ccdSearchService;

    private final CcdUpdateService ccdUpdateService;
    private final AuthTokenGenerator authTokenGenerator;

    private final List<CaseNotification> caseNotifications;

    @Autowired
    public  SystemUpdateCaseNotificationsTask(AuthTokenGenerator authTokenGenerator, CcdSearchService ccdSearchService,
                                              CcdUpdateService ccdUpdateService, IdamService idamService, List<CaseNotification> caseNotifications) {
        this.authTokenGenerator = authTokenGenerator;
        this.ccdSearchService = ccdSearchService;
        this.ccdUpdateService = ccdUpdateService;
        this.idamService = idamService;
        this.caseNotifications = caseNotifications;
    }

    @Override
    public void run() {
        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuthorization = authTokenGenerator.generate();

        try {
            updateCaseNotifications(user, serviceAuthorization);
            log.info("Update Case Notifications task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error("Update Case Notifications task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("Update Case Notifications task stopping "
                + "due to conflict with another running task"
            );
        }
    }

    private void updateCaseNotifications(User user, String serviceAuthorization) {
        // Query case that just sent notifications = true

        // Apply -> add case notifications to returned case
        // Apply -> set just sent notification to false

    }
}
