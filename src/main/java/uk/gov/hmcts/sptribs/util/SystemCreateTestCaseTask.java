package uk.gov.hmcts.sptribs.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.idam.CICUser;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.sptribs.systemupdate.service.CcdUpdateService;

import static uk.gov.hmcts.sptribs.systemupdate.event.SystemCreateTestCase.SYSTEM_CREATE_TEST_CASE;

@Component
@Slf4j
@RequiredArgsConstructor
public class SystemCreateTestCaseTask implements Runnable {

    private final IdamService idamService;
    private final AuthTokenGenerator authTokenGenerator;
    private final CcdUpdateService ccdUpdateService;


    @Value("${feature.create-system-update-test-case.enabled}")
    private boolean createTestCaseEnabled;

    @Value("${feature.create-system-update-test-case.count}")
    private int testCaseCount;

    @Override
    public void run() {
        if (!createTestCaseEnabled) {
            log.info("SystemCreateTestCaseTask is disabled. Skipping execution.");
            return;
        }

        log.info("SystemCreateTestCaseTask started. Attempting to create {} test case(s).", testCaseCount);

        final CICUser user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        int successCount = 0;
        int failureCount = 0;

        for (int i = 0; i < testCaseCount; i++) {
            try {
                triggerSystemCreateTestCase(user, serviceAuth);
                successCount++;
            } catch (RuntimeException e) {
                log.error("Failed to create test case on iteration {}. Error: {}", i + 1, e.getMessage());
                failureCount++;
            }
        }

        log.info(
            "SystemCreateTestCaseTask completed. Successfully created: {} case(s), Failed: {} case(s).",
            successCount, failureCount
        );
    }


    private void triggerSystemCreateTestCase(CICUser user, String serviceAuth) {
        try {
            ccdUpdateService.createCase(SYSTEM_CREATE_TEST_CASE, user, serviceAuth);

        } catch (final CcdManagementException e) {
            log.error("Create test case event failed");
        } catch (final IllegalArgumentException e) {
            log.error("Deserialization failed for create test case event");
        }
    }
}
