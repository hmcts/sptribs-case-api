package uk.gov.hmcts.sptribs.testutil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FunctionalTestDataManager {

    private static final Logger log = LoggerFactory.getLogger(FunctionalTestDataManager.class);

    private final DatastoreDatabaseManager datastoreManager;
    private final SptribsDatabaseManager   sptribsManager;

    private static final List<Long> testReferences = Collections.synchronizedList(new ArrayList<>());

    public FunctionalTestDataManager(
        String datastoreUrl,
        String spTribsUrl,
        String username,
        String password)
        throws SQLException {

        this.datastoreManager = new DatastoreDatabaseManager(datastoreUrl, username, password);
        this.sptribsManager   = new SptribsDatabaseManager(spTribsUrl, username, password);


        datastoreManager.connect();
        sptribsManager.connect();
    }

    public static FunctionalTestDataManager fromEnvironment() throws SQLException {
        log.info("Building FunctionalTestDataManager from environment variables.");

        String host     = requireSecret("DB_HOST");
        String port     = requireSecret("DB_PORT");
        String username = requireSecret("DB_USERNAME");
        String password = "DB_PASSWORD";

        String datastoreUrl = String.format("jdbc:postgresql://%s:%s/datastore", host, port);
        String sptribsUrl   = String.format("jdbc:postgresql://%s:%s/sptribs",   host, port);

        return new FunctionalTestDataManager(datastoreUrl, sptribsUrl, username, password);
    }

    private static String requireSecret(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                "Required environment variable '" + name + "' is not set or is blank.");
        }
        return value;
    }

    public void clearDown(long reference) throws SQLException {
        log.info("Starting clearDown for reference: {}", reference);

        //MUST RUN BEFORE DELETE CASE DATA AS DEPENDENT ON DATA IN TABLE
        datastoreManager.deleteCaseEvent(reference);

        datastoreManager.deleteCaseData(reference);

        sptribsManager.deleteCaseCorrespondences(reference);

        log.info("clearDown completed for reference: {}", reference);
    }


    public void closeAll() {
        log.info("Closing all database connections.");
        datastoreManager.close();
        sptribsManager.close();
    }

    public void addReference(Long id) {
        testReferences.add(id);
    }

    public List<Long> getTestReferences() {
        return Collections.unmodifiableList(testReferences);
    }
}
