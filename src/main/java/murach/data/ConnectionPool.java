package murach.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionPool {

    private static ConnectionPool pool = null;

    private ConnectionPool() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        }
        // Tự tạo bảng nếu chưa có
        Connection c = getConnection();
        if (c != null) {
            try (Statement s = c.createStatement()) {
                s.executeUpdate("CREATE TABLE IF NOT EXISTS `User` ("
                        + "Email VARCHAR(255) NOT NULL PRIMARY KEY,"
                        + "FirstName VARCHAR(100),"
                        + "LastName VARCHAR(100))");
            } catch (SQLException e) {
                System.out.println(e);
            } finally {
                freeConnection(c);
            }
        }
    }

    public static synchronized ConnectionPool getInstance() {
        if (pool == null) {
            pool = new ConnectionPool();
        }
        return pool;
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(
                    System.getenv("DB_URL"),
                    System.getenv("DB_USER"),
                    System.getenv("DB_PASS"));
        } catch (SQLException e) {
            System.out.println(e);
            return null;
        }
    }

    public void freeConnection(Connection c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
}