package uk.gov.hmcts.sptribs.systemupdate;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.sptribs.systemupdate.schedule.migration.SystemMigrateCaseDocumentsDocumentTableTask;

@Profile("preview")
@RestController
@RequestMapping("/migration")
@RequiredArgsConstructor
public class SystemUpdateController {

    private final SystemMigrateCaseDocumentsDocumentTableTask systemMigrateCaseDocumentsDocumentTableTask;

    @PostMapping("/run")
    public ResponseEntity<String> runSystemUpdate(
        @RequestHeader("migration-secret") String secret) {
        if (!"sptribs".equals(secret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        systemMigrateCaseDocumentsDocumentTableTask.run();
        return ResponseEntity.ok("Document table migration started");
    }
}
