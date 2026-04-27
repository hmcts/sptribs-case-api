package uk.gov.hmcts.sptribs.testutil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class DatastoreDatabaseManager extends AbstractDatabaseManager{
    private static final Logger log = LoggerFactory.getLogger(DatastoreDatabaseManager.class);

    private static final String TABLE_CASE_DATA  = "public.case_data";

    private static final String TABLE_CASE_EVENT = "public.case_event";


    public DatastoreDatabaseManager(String datastoreJdbcUrl, String username, String password) {
        super(datastoreJdbcUrl, username, password);
    }


    public void deleteCaseData(long reference) throws SQLException {
        log.info("Deleting case data for reference: {}", reference);
        deleteByColumn(TABLE_CASE_DATA, "reference", reference);
    }


    public void deleteCaseEvent(long reference) {

        //TODO:  Find out what from Case_data is needed to query table for deleting events.


        // deleteByColumn(TABLE_CASE_EVENT, "<correct_column_name>", reference);
    }
}
