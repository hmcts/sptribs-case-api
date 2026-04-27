package uk.gov.hmcts.sptribs.testutil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class SptribsDatabaseManager extends AbstractDatabaseManager {

    private static final Logger log = LoggerFactory.getLogger(SptribsDatabaseManager.class);

    private static final String TABLE_CASE_CORRESPONDENCES = "public.case_correspondences";

    public SptribsDatabaseManager(String sptribsUrl, String username, String password) {
        super(sptribsUrl, username, password);
    }

    public void deleteCaseCorrespondences(long reference) throws SQLException {
        log.info("Deleting case correspondences for reference: {}", reference);
        deleteByColumn(TABLE_CASE_CORRESPONDENCES, "case_reference_number", reference);
    }
}
