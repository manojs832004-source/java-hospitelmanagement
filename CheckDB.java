import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckDB {
    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/AppointmentSystem", "root", "Manoj2114k");
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM Appointments;");
        System.out.println("ID | PatID | DocID | Date | Time | Status");
        while (rs.next()) {
            System.out.printf("%d | %s | %s | %s | %s | %s\n",
                rs.getInt("appointment_id"),
                rs.getString("patient_id"),
                rs.getString("doctor_id"),
                rs.getString("date"),
                rs.getString("time"),
                rs.getString("status")
            );
        }
        conn.close();
    }
}