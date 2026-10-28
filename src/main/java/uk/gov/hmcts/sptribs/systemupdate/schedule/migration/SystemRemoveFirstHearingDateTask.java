package uk.gov.hmcts.sptribs.systemupdate.schedule.migration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.SystemEventExecutor;
import uk.gov.hmcts.ccd.sdk.SystemEventResult;
import uk.gov.hmcts.sptribs.systemupdate.service.FirstHearingDateMigrationRepository;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_CASE_TYPE;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemMigrateCase.SYSTEM_MIGRATE_CASE;

@Component
@RequiredArgsConstructor
@Slf4j
public class SystemRemoveFirstHearingDateTask implements Runnable {

    private final FirstHearingDateMigrationRepository repository;
    private final ObjectProvider<SystemEventExecutor> systemEventExecutor;

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
        validateConfiguration();

        // The executor bean is only available when the SDK's system identity is configured.
        final SystemEventExecutor executor = systemEventExecutor.getObject();
        final Long selectedReference = selectedReference();
        final MigrationResult result = selectedReference == null
            ? migrateAllCases(executor) : migrateSelectedCase(selectedReference, executor);
        final long remaining = countRemaining(selectedReference);
        log.info("First hearing date removal: {} succeeded, {} failed, {} remaining",
            result.succeeded(), result.failed(), remaining);
        if (result.failed() > 0 || remaining > 0) {
            throw new IllegalStateException("First hearing date removal incomplete; check failed cases and retry");
        }
    }

    private void validateConfiguration() {
        if (!enabled) {
            throw new IllegalStateException("First hearing date removal is disabled");
        }
        if (batchSize < 1) {
            throw new IllegalArgumentException("First hearing date removal batch size must be positive");
        }
        if (batchPauseMs < 0) {
            throw new IllegalArgumentException("First hearing date removal batch pause must not be negative");
        }
    }

    private Long selectedReference() {
        return caseReference == null || caseReference.isBlank() ? null : Long.parseLong(caseReference.trim());
    }

    private MigrationResult migrateSelectedCase(long reference, SystemEventExecutor executor) {
        if (!repository.hasFirstHearingDate(reference)) {
            return new MigrationResult(0, 0);
        }
        return migrateReferences(List.of(reference), executor);
    }

    private MigrationResult migrateAllCases(SystemEventExecutor executor) {
        MigrationResult result = new MigrationResult(0, 0);
        long afterReference = 0;
        List<Long> references;
        do {
            references = repository.findNextBatch(afterReference, batchSize);
            result = result.plus(migrateReferences(references, executor));
            if (!references.isEmpty()) {
                afterReference = references.getLast();
                if (references.size() == batchSize) {
                    pauseBetweenBatches();
                }
            }
        } while (!references.isEmpty());
        return result;
    }

    private MigrationResult migrateReferences(List<Long> references, SystemEventExecutor executor) {
        int succeeded = 0;
        int failed = 0;
        for (Long reference : references) {
            if (clearAndVerify(reference, executor)) {
                succeeded++;
            } else {
                failed++;
            }
        }
        return new MigrationResult(succeeded, failed);
    }

    private long countRemaining(Long selectedReference) {
        if (selectedReference == null) {
            return repository.countRemaining();
        }
        return repository.hasFirstHearingDate(selectedReference) ? 1 : 0;
    }

    private record MigrationResult(int succeeded, int failed) {
        private MigrationResult plus(MigrationResult other) {
            return new MigrationResult(succeeded + other.succeeded, failed + other.failed);
        }
    }

    private boolean clearAndVerify(long reference, SystemEventExecutor executor) {
        try {
            executor.execute(reference, idempotencyKey(reference), context -> {
                if (!ST_CIC_CASE_TYPE.equals(context.caseTypeId())) {
                    throw new IllegalStateException("Unexpected case type for first hearing date removal");
                }
                if (!repository.removeFirstHearingDate(context.caseReference())) {
                    // Do not create an audit event when another run has already cleared the key.
                    throw new FirstHearingDateAlreadyRemovedException();
                }
                return SystemEventResult.withoutStateTransition(
                    SYSTEM_MIGRATE_CASE, "Migrate case data", "Remove retired firstHearingDate field");
            });
            if (!repository.hasFirstHearingDate(reference)) {
                return true;
            }
            log.error("First hearing date still present on case {} after system event", reference);
        } catch (FirstHearingDateAlreadyRemovedException e) {
            return !repository.hasFirstHearingDate(reference);
        } catch (RuntimeException e) {
            log.error("Could not remove first hearing date from case {}", reference, e);
        }
        return false;
    }

    private UUID idempotencyKey(long reference) {
        return UUID.nameUUIDFromBytes(("remove-first-hearing-date:v1:" + reference)
            .getBytes(StandardCharsets.UTF_8));
    }

    private static class FirstHearingDateAlreadyRemovedException extends RuntimeException {
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
