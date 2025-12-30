package com.hospital.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "patients")
@NamedQueries({
        @NamedQuery(name = "Patient.findAll", query = "SELECT p FROM Patient p ORDER BY p.lastName"),
        @NamedQuery(name = "Patient.findActive", query = "SELECT p FROM Patient p WHERE p.status = 'active' ORDER BY p.lastName"),
        @NamedQuery(name = "Patient.searchByName", query = "SELECT p FROM Patient p WHERE LOWER(p.firstName) LIKE LOWER(:search) OR LOWER(p.lastName) LIKE LOWER(:search)"),
        @NamedQuery(name = "Patient.findByEpaId", query = "SELECT p FROM Patient p WHERE p.epaId = :epaId"),

        // NEU: Hinzufügen
        @NamedQuery(name = "Patient.findRandom", query = "SELECT p FROM Patient p ORDER BY function('RANDOM')"),

        @NamedQuery(name = "Patient.searchByNameOptimized", query = "SELECT p FROM Patient p WHERE " +
                "LOWER(CONCAT(p.firstName, ' ', p.lastName)) LIKE LOWER(:search) " +
                "ORDER BY p.lastName, p.firstName"),

        @NamedQuery(name = "Patient.countByNameSearch", query = "SELECT COUNT(p) FROM Patient p WHERE " +
                "LOWER(CONCAT(p.firstName, ' ', p.lastName)) LIKE LOWER(:search)")
})
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false, length = 20)
    private String gender;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "insurance_number", unique = true, nullable = false, length = 50)
    private String insuranceNumber;

    @Column(name = "blood_type", length = 5)
    private String bloodType;

    @Column(columnDefinition = "TEXT")
    private String allergies;

    @Column(name = "emergency_contact_name", length = 100)
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;

    @Column(name = "admission_date")
    private LocalDateTime admissionDate;

    @Column(name = "discharge_date")
    private LocalDateTime dischargeDate;

    @Column(length = 20)
    private String status = "active";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // EPA-spezifische Felder
    @Column(name = "epa_id", unique = true, length = 100)
    private String epaId; // ID in der elektronischen Patientenakte

    @Column(name = "epa_sync_status", length = 20)
    private String epaSyncStatus = "pending"; // pending, synced, error

    @Column(name = "epa_last_sync")
    private LocalDateTime epaLastSync;

    @Column(name = "epa_sync_error", columnDefinition = "TEXT")
    private String epaSyncError;

    @Column(name = "epa_enabled")
    private Boolean epaEnabled = true; // Ob Patient EPA-Synchronisation erlaubt

    @Column(name = "epa_consent_date")
    private LocalDateTime epaConsentDate; // Wann Patient Einwilligung gegeben hat

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Diagnosis> diagnoses;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Medication> medications;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (admissionDate == null) {
            admissionDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Bestehende Getters und Setters...
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getInsuranceNumber() {
        return insuranceNumber;
    }

    public void setInsuranceNumber(String insuranceNumber) {
        this.insuranceNumber = insuranceNumber;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }

    public LocalDateTime getAdmissionDate() {
        return admissionDate;
    }

    public void setAdmissionDate(LocalDateTime admissionDate) {
        this.admissionDate = admissionDate;
    }

    public LocalDateTime getDischargeDate() {
        return dischargeDate;
    }

    public void setDischargeDate(LocalDateTime dischargeDate) {
        this.dischargeDate = dischargeDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @JsonbTransient
    public List<Diagnosis> getDiagnoses() {
        return diagnoses;
    }

    public void setDiagnoses(List<Diagnosis> diagnoses) {
        this.diagnoses = diagnoses;
    }

    @JsonbTransient
    public List<Medication> getMedications() {
        return medications;
    }

    public void setMedications(List<Medication> medications) {
        this.medications = medications;
    }

    // EPA-spezifische Getters und Setters
    public String getEpaId() {
        return epaId;
    }

    public void setEpaId(String epaId) {
        this.epaId = epaId;
    }

    public String getEpaSyncStatus() {
        return epaSyncStatus;
    }

    public void setEpaSyncStatus(String epaSyncStatus) {
        this.epaSyncStatus = epaSyncStatus;
    }

    public LocalDateTime getEpaLastSync() {
        return epaLastSync;
    }

    public void setEpaLastSync(LocalDateTime epaLastSync) {
        this.epaLastSync = epaLastSync;
    }

    public String getEpaSyncError() {
        return epaSyncError;
    }

    public void setEpaSyncError(String epaSyncError) {
        this.epaSyncError = epaSyncError;
    }

    public Boolean getEpaEnabled() {
        return epaEnabled;
    }

    public void setEpaEnabled(Boolean epaEnabled) {
        this.epaEnabled = epaEnabled;
    }

    public LocalDateTime getEpaConsentDate() {
        return epaConsentDate;
    }

    public void setEpaConsentDate(LocalDateTime epaConsentDate) {
        this.epaConsentDate = epaConsentDate;
    }
}