-- Datenbank erstellen
CREATE DATABASE hospital_db;

-- Mit der Datenbank verbinden
\c hospital_db;

-- Patienten Tabelle
CREATE TABLE patients (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(20) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    insurance_number VARCHAR(50) UNIQUE NOT NULL,
    blood_type VARCHAR(5),
    allergies TEXT,
    emergency_contact_name VARCHAR(100),
    emergency_contact_phone VARCHAR(20),
    admission_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    discharge_date TIMESTAMP,
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Diagnosen Tabelle
CREATE TABLE diagnoses (
    id SERIAL PRIMARY KEY,
    patient_id INTEGER REFERENCES patients(id) ON DELETE CASCADE,
    diagnosis_code VARCHAR(20),
    diagnosis_name VARCHAR(200) NOT NULL,
    diagnosis_date DATE NOT NULL,
    doctor_name VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Medikamente Tabelle
CREATE TABLE medications (
    id SERIAL PRIMARY KEY,
    patient_id INTEGER REFERENCES patients(id) ON DELETE CASCADE,
    medication_name VARCHAR(200) NOT NULL,
    dosage VARCHAR(100),
    frequency VARCHAR(100),
    start_date DATE NOT NULL,
    end_date DATE,
    prescribed_by VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index für bessere Performance
CREATE INDEX idx_patients_last_name ON patients(last_name);
CREATE INDEX idx_patients_insurance ON patients(insurance_number);
CREATE INDEX idx_diagnoses_patient ON diagnoses(patient_id);
CREATE INDEX idx_medications_patient ON medications(patient_id);

-- Trigger für updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_patients_updated_at BEFORE UPDATE ON patients
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Beispieldaten
INSERT INTO patients (first_name, last_name, date_of_birth, gender, phone, email, address, insurance_number, blood_type, allergies) VALUES
('Anna', 'Müller', '1985-03-15', 'Weiblich', '030-12345678', 'anna.mueller@email.de', 'Hauptstraße 123, 10115 Berlin', 'INS-2024-001', 'A+', 'Penicillin'),
('Max', 'Schmidt', '1992-07-22', 'Männlich', '030-87654321', 'max.schmidt@email.de', 'Nebenstraße 45, 10117 Berlin', 'INS-2024-002', 'O+', 'Keine'),
('Sophie', 'Weber', '1978-11-30', 'Weiblich', '030-55555555', 'sophie.weber@email.de', 'Parkweg 7, 10119 Berlin', 'INS-2024-003', 'B-', 'Aspirin');