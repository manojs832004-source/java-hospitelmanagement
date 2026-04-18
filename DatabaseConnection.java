import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Database credentials and URL
    private static final String DB_URL = "jdbc:mysql://localhost:3306/AppointmentSystem";
    private static final String USER = "root"; // connecting as root@localhost
    private static final String PASS = "Manoj2114k"; // ❌ ENTER YOUR ACTUAL MYSQL ROOT PASSWORD HERE! ❌

    // Method to establish and get connection
    public static Connection getConnection() {
        Connection connection = null;
        try {
            // Load the MySQL JDBC driver (Optional since JDBC 4.0, but good practice)
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Connection to MySQL established successfully!");
        } catch (SQLException e) {
            System.err.println("Failed to connect to MySQL database.");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
        }
        return connection;
    }
}