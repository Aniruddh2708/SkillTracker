package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    /*
     * JDBC configuration (override with environment variables if needed):
     *   SKILLBRIDGE_DB_URL      (default: jdbc:mysql://localhost:3306/skillbridge)
     *   SKILLBRIDGE_DB_USER     (default: root — typical local MySQL)
     *   SKILLBRIDGE_DB_PASSWORD (default: empty — set this if your root user has a password)
     *
     * If login shows "database error", MySQL is not reachable or credentials/schema are wrong.
     */
    private static final String URL =
            System.getenv().getOrDefault("SKILLBRIDGE_DB_URL", "jdbc:mysql://localhost:3306/skillbridge");
    private static final String DB_USER =
            System.getenv().getOrDefault("SKILLBRIDGE_DB_USER", "root");
    private static final String PASSWORD =
            System.getenv().getOrDefault("SKILLBRIDGE_DB_PASSWORD", "");

    // Single shared connection instance for the app lifetime (simple singleton).
    private static volatile Connection instance = null;

    private DBConnection() { }

    public static Connection getInstance() throws SQLException {
        // Re-open when first requested or after an explicit close.
        if (instance == null || instance.isClosed()) {
            instance = DriverManager.getConnection(URL, DB_USER, PASSWORD);
        }
        return instance;
    }

    public static void close() {
        if (instance != null) {
            try {
                instance.close();
            } catch (SQLException e) {
                System.err.println("Warning: could not close DB connection - " + e.getMessage());
            }
        }
    }
}
