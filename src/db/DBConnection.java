package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/skillbridge";
    private static final String DB_USER  = "root";
    private static final String PASSWORD = "De@dp00l";

    private static volatile Connection instance = null;

    private DBConnection() { }

    public static Connection getInstance() throws SQLException {
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
                System.err.println("Warning: could not close DB connection — " + e.getMessage());
            }
        }
    }
}
