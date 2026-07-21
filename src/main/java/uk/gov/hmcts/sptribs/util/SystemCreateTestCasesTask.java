package uk.gov.hmcts.sptribs.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdUpdateService;

import static uk.gov.hmcts.sptribs.systemupdate.event.SystemCreateTestCases.SYSTEM_CREATE_TEST_CASES;

@Component
@Slf4j
@RequiredArgsConstructor
public class SystemCreateTestCasesTask implements Runnable {

    private final IdamService idamService;
    private final AuthTokenGenerator authTokenGenerator;
    private final CcdUpdateService ccdUpdateService;


    @Value("${feature.create-system-update-test-cases.enabled}")
    private boolean createTestCasesEnabled;

    @Value("${feature.create-system-update-test-cases.count}")
    private int testCaseCount;

    @Override
    public void run() {
        if (!createTestCasesEnabled) {
            log.info("SystemCreateTestCasesTask is disabled. Skipping execution.");
            return;
        }

        log.info("SystemCreateTestCasesTask started. Attempting to create {} test case(s).", testCaseCount);

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        int successCount = 0;
        int failureCount = 0;

        for (int i = 0; i < testCaseCount; i++) {
            try {
                triggerSystemCreateTestCases(user, serviceAuth);
                successCount++;
            } catch (RuntimeException e) {
                log.error("Failed to create test case on iteration {}. Error: {}", i + 1, e.getMessage());
                failureCount++;
            }
        }

        log.info(
            "SystemCreateTestCasesTask completed. Successfully created: {} case(s), Failed: {} case(s).",
            successCount, failureCount
        );
    }


    private void triggerSystemCreateTestCases(User user, String serviceAuth) {
        try {
            ccdUpdateService.createCase(SYSTEM_CREATE_TEST_CASES, user, serviceAuth);

        } catch (final CcdManagementException e) {
            log.error("Create test case event failed");
        } catch (final IllegalArgumentException e) {
            log.error("Deserialization failed for create test case event");
        }
    }
}
