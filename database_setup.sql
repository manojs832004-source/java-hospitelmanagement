CREATE DATABASE IF NOT EXISTS AppointmentSystem;
USE AppointmentSystem;

CREATE TABLE Doctors (
    doctor_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    specialty VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE Patients (
    patient_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INT,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE Appointments (
    appointment_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id VARCHAR(50),
    doctor_id VARCHAR(50),
    date DATE NOT NULL,
    time TIME NOT NULL,
    reason TEXT,
    status VARCHAR(20) DEFAULT 'Pending',
    FOREIGN KEY (patient_id) REFERENCES Patients(patient_id),
    FOREIGN KEY (doctor_id) REFERENCES Doctors(doctor_id)
);

private static final String PASS = "Manoj2114k";
