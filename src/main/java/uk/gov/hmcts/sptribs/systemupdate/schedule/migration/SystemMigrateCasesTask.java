package uk.gov.hmcts.sptribs.systemupdate.schedule.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.systemupdate.schedule.migration.Migration;

import java.util.List;

import static java.util.Comparator.comparing;

@Component
@Slf4j
public class SystemMigrateCasesTask implements Runnable {

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private List<Migration> migrations;

    @Override
    public void run() {
        log.info("Migrate cases scheduled task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuthorization = authTokenGenerator.generate();

        migrations.stream()
                .sorted(comparing(Migration::getPriority)) //Ascending priority, 0 (zero) is highest
                .forEach(migration -> migration.apply(user, serviceAuthorization));

        log.info("Migrate cases scheduled task finished");
    }
}