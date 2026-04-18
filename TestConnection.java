import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestConnection {
    public static void main(String[] args) {
        String dbUrl = "jdbc:mysql://localhost:3306/AppointmentSystem";
        String user = "root";
        String pass = "";
        try {
            Connection connection = DriverManager.getConnection(dbUrl, user, pass);
            System.out.println("Connection successful");
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}