package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/foodshare_db";

    private static final String USER = "foodshare_user";
    private static final String PASSWORD = "foodshareTemp123!";

    public static Connection getConnection()
            throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
