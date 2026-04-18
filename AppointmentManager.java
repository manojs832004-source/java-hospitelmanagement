import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AppointmentManager {
    
    // Register a new doctor
    public static boolean registerDoctor(String doctorId, String name, String specialty, String password) {
        String query = "INSERT INTO Doctors (doctor_id, name, specialty, password) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, doctorId);
            stmt.setString(2, name);
            stmt.setString(3, specialty);
            stmt.setString(4, password); // Note: In production, hash the password!
            
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Login doctor
    public static Doctor loginDoctor(String doctorId, String password) {
        String query = "SELECT * FROM Doctors WHERE doctor_id = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, doctorId);
            stmt.setString(2, password);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Doctor(
                    rs.getString("doctor_id"),
                    rs.getString("name"),
                    rs.getString("specialty"),
                    rs.getString("password")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Get all doctors
    public static List<Doctor> getAllDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        String query = "SELECT * FROM Doctors";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                doctors.add(new Doctor(
                    rs.getString("doctor_id"),
                    rs.getString("name"),
                    rs.getString("specialty"),
                    rs.getString("password")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return doctors;
    }
    
    // Get doctor by ID
    public static Doctor getDoctorById(String doctorId) {
        String query = "SELECT * FROM Doctors WHERE doctor_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Doctor(
                    rs.getString("doctor_id"),
                    rs.getString("name"),
                    rs.getString("specialty"),
                    rs.getString("password")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Patient Registration
    public static boolean registerPatient(String patientId, String name, int age, String password) {
        String query = "INSERT INTO Patients (patient_id, name, age, password) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, patientId);
            stmt.setString(2, name);
            stmt.setInt(3, age);
            stmt.setString(4, password);
            
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Patient Login
    // Note: Assuming we return a simple Patient object, we'll create that class below
    public static Patient loginPatient(String patientId, String password) {
        String query = "SELECT * FROM Patients WHERE patient_id = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, patientId);
            stmt.setString(2, password);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Patient(
                    rs.getString("patient_id"),
                    rs.getString("name"),
                    rs.getInt("age"),
                    rs.getString("password")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Create appointment request
    public static AppointmentRequest createRequest(String patientId, String patientName, 
                                                    String doctorId, String date, 
                                                    String time, String reason) {
        String query = "INSERT INTO Appointments (patient_id, doctor_id, date, time, reason, status) VALUES (?, ?, ?, ?, ?, 'Pending')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, patientId);
            stmt.setString(2, doctorId);
            stmt.setString(3, date);
            stmt.setString(4, time);
            stmt.setString(5, reason);
            
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int appointmentId = rs.getInt(1);
                return new AppointmentRequest(
                    String.valueOf(appointmentId),
                    patientId,
                    patientName,
                    doctorId,
                    date,
                    time,
                    reason,
                    "Pending"
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Get requests for a specific doctor
    public static List<AppointmentRequest> getRequestsForDoctor(String doctorId) {
        List<AppointmentRequest> requests = new ArrayList<>();
        // Join with Patients table to get the patient's name
        String query = "SELECT a.*, p.name AS patient_name FROM Appointments a JOIN Patients p ON a.patient_id = p.patient_id WHERE a.doctor_id = ? AND a.status = 'Pending'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                requests.add(new AppointmentRequest(
                    String.valueOf(rs.getInt("appointment_id")),
                    rs.getString("patient_id"),
                    rs.getString("patient_name"),
                    rs.getString("doctor_id"),
                    rs.getString("date"),
                    rs.getString("time"),
                    rs.getString("reason"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }
    
    // Get requests by a patient
    public static List<AppointmentRequest> getRequestsByPatient(String patientId) {
        List<AppointmentRequest> requests = new ArrayList<>();
        String query = "SELECT a.*, p.name AS patient_name FROM Appointments a JOIN Patients p ON a.patient_id = p.patient_id WHERE a.patient_id = ? AND a.status = 'Pending'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, patientId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                requests.add(new AppointmentRequest(
                    String.valueOf(rs.getInt("appointment_id")),
                    rs.getString("patient_id"),
                    rs.getString("patient_name"),
                    rs.getString("doctor_id"),
                    rs.getString("date"),
                    rs.getString("time"),
                    rs.getString("reason"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }
    
    // Accept appointment request
    public static boolean acceptRequest(String requestId) {
        String query = "UPDATE Appointments SET status = 'Scheduled' WHERE appointment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, Integer.parseInt(requestId));
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Reject appointment request
    public static boolean rejectRequest(String requestId) {
        String query = "UPDATE Appointments SET status = 'Rejected' WHERE appointment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, Integer.parseInt(requestId));
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Get appointments for a doctor (Scheduled/Completed)
    public static List<Appointment> getAppointmentsForDoctor(String doctorId) {
        List<Appointment> appointments = new ArrayList<>();
        String query = "SELECT a.*, p.name AS patient_name FROM Appointments a JOIN Patients p ON a.patient_id = p.patient_id WHERE a.doctor_id = ? AND a.status != 'Pending' AND a.status != 'Rejected'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                appointments.add(new Appointment(
                    String.valueOf(rs.getInt("appointment_id")),
                    rs.getString("patient_id"),
                    rs.getString("patient_name"),
                    rs.getString("doctor_id"),
                    rs.getString("date"),
                    rs.getString("time"),
                    rs.getString("reason"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appointments;
    }
    
    // Get appointments for a patient (Scheduled/Completed)
    public static List<Appointment> getAppointmentsForPatient(String patientId) {
        List<Appointment> appointments = new ArrayList<>();
        String query = "SELECT a.*, p.name AS patient_name FROM Appointments a JOIN Patients p ON a.patient_id = p.patient_id WHERE a.patient_id = ? AND a.status != 'Pending' AND a.status != 'Rejected'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, patientId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                appointments.add(new Appointment(
                    String.valueOf(rs.getInt("appointment_id")),
                    rs.getString("patient_id"),
                    rs.getString("patient_name"),
                    rs.getString("doctor_id"),
                    rs.getString("date"),
                    rs.getString("time"),
                    rs.getString("reason"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appointments;
    }
    
    // Inner classes for data models
    public static class Patient {
        public String id;
        public String name;
        public int age;
        public String password;
        
        public Patient(String id, String name, int age, String password) {
            this.id = id;
            this.name = name;
            this.age = age;
            this.password = password;
        }
    }
    public static class Doctor {
        public String id;
        public String name;
        public String specialty;
        public String password;
        
        public Doctor(String id, String name, String specialty, String password) {
            this.id = id;
            this.name = name;
            this.specialty = specialty;
            this.password = password;
        }
    }
    
    public static class AppointmentRequest {
        public String id;
        public String patientId;
        public String patientName;
        public String doctorId;
        public String date;
        public String time;
        public String reason;
        public String status; // PENDING, ACCEPTED, REJECTED
        public String createdAt;
        
        public AppointmentRequest(String id, String patientId, String patientName,
                                 String doctorId, String date, String time,
                                 String reason, String status) {
            this.id = id;
            this.patientId = patientId;
            this.patientName = patientName;
            this.doctorId = doctorId;
            this.date = date;
            this.time = time;
            this.reason = reason;
            this.status = status;
            this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }
    
    public static class Appointment {
        public String id;
        public String patientId;
        public String patientName;
        public String doctorId;
        public String date;
        public String time;
        public String reason;
        public String status; // SCHEDULED, COMPLETED, CANCELLED
        public String confirmedAt;
        
        public Appointment(String id, String patientId, String patientName,
                          String doctorId, String date, String time,
                          String reason, String status) {
            this.id = id;
            this.patientId = patientId;
            this.patientName = patientName;
            this.doctorId = doctorId;
            this.date = date;
            this.time = time;
            this.reason = reason;
            this.status = status;
            this.confirmedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }
}
