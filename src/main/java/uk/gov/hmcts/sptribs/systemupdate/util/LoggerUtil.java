package uk.gov.hmcts.sptribs.systemupdate.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class LoggerUtil {

    private LoggerUtil() {
    }

    public static void logMigrationError(final String message, final Exception e) {
        log.error(message);
    }
}
