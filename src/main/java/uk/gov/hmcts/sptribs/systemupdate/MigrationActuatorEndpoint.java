package uk.gov.hmcts.sptribs.systemupdate;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.systemupdate.schedule.migration.SystemMigrateCaseDocumentsDocumentTableTask;

@Component
@Endpoint(id = "migration")
@Profile("preview")
@RequiredArgsConstructor
public class MigrationActuatorEndpoint {

    private final SystemMigrateCaseDocumentsDocumentTableTask migrationTask;

    @WriteOperation
    public String runMigration() {
        migrationTask.run();
        return "Migration started";
    }
}
