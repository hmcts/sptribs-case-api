package uk.gov.hmcts.sptribs.systemupdate.schedule.migration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.idam.CICUser;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.sptribs.systemupdate.service.FirstHearingDateMigrationRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SystemRemoveFirstHearingDateTask implements Runnable {

    private final AuthTokenGenerator authTokenGenerator;
    private final IdamService idamService;
    private final FirstHearingDateMigrationRepository repository;
    private final CcdUpdateService ccdUpdateService;

    @Value("${feature.remove-first-hearing-date-task.enabled:false}")
    private boolean enabled;

    @Value("${feature.remove-first-hearing-date-task.caseReference:}")
    private String caseReference;

    @Value("${feature.remove-first-hearing-date-task.batchSize:100}")
    private int batchSize;

    @Value("${feature.remove-first-hearing-date-task.batchPauseMs:5000}")
    private long batchPauseMs;

    @Override
    public void run() {
        if (!enabled) {
            throw new IllegalStateException("First hearing date removal is disabled");
        }
        if (batchSize < 1) {
            throw new IllegalArgumentException("First hearing date removal batch size must be positive");
        }
        if (batchPauseMs < 0) {
            throw new IllegalArgumentException("First hearing date removal batch pause must not be negative");
        }

        final CICUser user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();
        int succeeded = 0;
        int failed = 0;

        if (caseReference != null && !caseReference.isBlank()) {
            final long reference = Long.parseLong(caseReference.trim());
            if (repository.hasFirstHearingDate(reference)) {
                if (clearAndVerify(reference, user, serviceAuth)) {
                    succeeded++;
                } else {
                    failed++;
                }
            }
        } else {
            long afterReference = 0;
            List<Long> references;
            do {
                references = repository.findNextBatch(afterReference, batchSize);
                for (Long reference : references) {
                    if (clearAndVerify(reference, user, serviceAuth)) {
                        succeeded++;
                    } else {
                        failed++;
                    }
                }
                if (!references.isEmpty()) {
                    afterReference = references.getLast();
                    if (references.size() == batchSize) {
                        pauseBetweenBatches();
                    }
                }
            } while (!references.isEmpty());
        }

        final long remaining = caseReference == null || caseReference.isBlank()
            ? repository.countRemaining() : (repository.hasFirstHearingDate(Long.parseLong(caseReference.trim())) ? 1 : 0);
        log.info("First hearing date removal: {} succeeded, {} failed, {} remaining", succeeded, failed, remaining);
        if (failed > 0 || remaining > 0) {
            throw new IllegalStateException("First hearing date removal incomplete; check failed cases and retry");
        }
    }

    private boolean clearAndVerify(long reference, CICUser user, String serviceAuth) {
        try {
            ccdUpdateService.clearFirstHearingDate(reference, user, serviceAuth);
            if (!repository.hasFirstHearingDate(reference)) {
                return true;
            }
            log.error("First hearing date still present on case {} after CCD update", reference);
        } catch (RuntimeException e) {
            log.error("Could not remove first hearing date from case {}", reference, e);
        }
        return false;
    }

    private void pauseBetweenBatches() {
        try {
            Thread.sleep(batchPauseMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("First hearing date removal interrupted", e);
        }
    }
}
