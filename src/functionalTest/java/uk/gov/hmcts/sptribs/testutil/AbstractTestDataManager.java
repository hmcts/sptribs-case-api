package uk.gov.hmcts.sptribs.testutil;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

abstract class AbstractDatabaseManager {

    private static final Logger log = LoggerFactory.getLogger(AbstractDatabaseManager.class);

    private final String jdbcUrl;
    private final String username;
    private final String password;

    protected Connection connection;

    protected AbstractDatabaseManager(String jdbcUrl, String username, String password) {
        this.jdbcUrl  = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    public void connect() throws SQLException {
        log.info("Connecting to database: {}", jdbcUrl);
        try {
            connection = DriverManager.getConnection(jdbcUrl, username, password);
            log.info("Database connection established successfully.");
        } catch (SQLException e) {
            log.error("Failed to connect to database: {}", jdbcUrl, e);
            throw e;
        }
    }

    public void deleteByColumn(String table, String column, Object value) throws SQLException {
        // Build the DELETE statement – table/column are structural identifiers embedded
        // directly in the SQL string; value is bound via a PreparedStatement parameter.
        String sql = String.format("DELETE FROM %s WHERE %s = ?", table, column);
        log.info("Executing: {}  [value={}]", sql, value);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, value);
            int rowsAffected = statement.executeUpdate();
            log.info("Deleted {} row(s) from {} where {} = '{}'.", rowsAffected, table, column, value);

        } catch (SQLException e) {
            log.error("Error executing DELETE on table '{}', column '{}', value '{}'.",
                table, column, value, e);
            throw e;
        }
    }

    public void close() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    log.info("Connection to {} closed.", jdbcUrl);
                }
            } catch (SQLException e) {
                log.error("Error while closing the database connection.", e);
            }
        }
    }
}
