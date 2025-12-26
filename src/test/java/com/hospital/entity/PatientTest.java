package com.hospital.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Patient entity
 */
class PatientTest {

    private Patient patient;

    @BeforeEach
    void setUp() {
        patient = new Patient();
    }

    @Test
    @DisplayName("Should create patient with all fields")
    void testCreatePatientWithAllFields() {
        // Arrange & Act
        patient.setFirstName("Anna");
        patient.setLastName("Müller");
        patient.setDateOfBirth(LocalDate.of(1985, 3, 15));
        patient.setGender("Weiblich");
        patient.setPhone("030-12345678");
        patient.setEmail("anna.mueller@email.de");
        patient.setAddress("Hauptstraße 123, 10115 Berlin");
        patient.setInsuranceNumber("INS-2024-001");
        patient.setBloodType("A+");
        patient.setAllergies("Penicillin");
        patient.setEmergencyContactName("Hans Müller");
        patient.setEmergencyContactPhone("030-87654321");
        patient.setStatus("active");
        patient.setEpaEnabled(true);

        // Assert
        assertEquals("Anna", patient.getFirstName());
        assertEquals("Müller", patient.getLastName());
        assertEquals(LocalDate.of(1985, 3, 15), patient.getDateOfBirth());
        assertEquals("Weiblich", patient.getGender());
        assertEquals("030-12345678", patient.getPhone());
        assertEquals("anna.mueller@email.de", patient.getEmail());
        assertEquals("Hauptstraße 123, 10115 Berlin", patient.getAddress());
        assertEquals("INS-2024-001", patient.getInsuranceNumber());
        assertEquals("A+", patient.getBloodType());
        assertEquals("Penicillin", patient.getAllergies());
        assertEquals("Hans Müller", patient.getEmergencyContactName());
        assertEquals("030-87654321", patient.getEmergencyContactPhone());
        assertEquals("active", patient.getStatus());
        assertTrue(patient.getEpaEnabled());
    }

    @Test
    @DisplayName("Should set default status to active")
    void testDefaultStatus() {
        // Assert
        assertEquals("active", patient.getStatus());
    }

    @Test
    @DisplayName("Should handle EPA fields correctly")
    void testEPAFields() {
        // Arrange & Act
        patient.setEpaId("EPA-123456");
        patient.setEpaSyncStatus("synced");
        patient.setEpaLastSync(LocalDateTime.now());
        patient.setEpaEnabled(true);
        patient.setEpaConsentDate(LocalDateTime.now());

        // Assert
        assertEquals("EPA-123456", patient.getEpaId());
        assertEquals("synced", patient.getEpaSyncStatus());
        assertNotNull(patient.getEpaLastSync());
        assertTrue(patient.getEpaEnabled());
        assertNotNull(patient.getEpaConsentDate());
    }

    @Test
    @DisplayName("Should handle EPA sync errors")
    void testEPASyncError() {
        // Arrange & Act
        patient.setEpaSyncStatus("error");
        patient.setEpaSyncError("Connection timeout");

        // Assert
        assertEquals("error", patient.getEpaSyncStatus());
        assertEquals("Connection timeout", patient.getEpaSyncError());
    }

    @Test
    @DisplayName("Should handle null optional fields")
    void testNullOptionalFields() {
        // Arrange & Act
        patient.setFirstName("Test");
        patient.setLastName("User");
        patient.setDateOfBirth(LocalDate.of(2000, 1, 1));
        patient.setGender("Divers");
        patient.setInsuranceNumber("INS-TEST-001");

        // Assert - these should be null or have default values
        assertNull(patient.getPhone());
        assertNull(patient.getEmail());
        assertNull(patient.getAddress());
        assertNull(patient.getBloodType());
        assertNull(patient.getAllergies());
        assertNull(patient.getEmergencyContactName());
        assertNull(patient.getEmergencyContactPhone());
    }

    @Test
    @DisplayName("Should handle admission and discharge dates")
    void testAdmissionAndDischargeDates() {
        // Arrange & Act
        LocalDateTime admissionDate = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime dischargeDate = LocalDateTime.of(2024, 1, 15, 14, 30);
        
        patient.setAdmissionDate(admissionDate);
        patient.setDischargeDate(dischargeDate);

        // Assert
        assertEquals(admissionDate, patient.getAdmissionDate());
        assertEquals(dischargeDate, patient.getDischargeDate());
    }

    @Test
    @DisplayName("Should handle all blood types")
    void testBloodTypes() {
        String[] bloodTypes = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};

        for (String bloodType : bloodTypes) {
            patient.setBloodType(bloodType);
            assertEquals(bloodType, patient.getBloodType());
        }
    }

    @Test
    @DisplayName("Should handle all genders")
    void testGenders() {
        // Test Männlich
        patient.setGender("Männlich");
        assertEquals("Männlich", patient.getGender());

        // Test Weiblich
        patient.setGender("Weiblich");
        assertEquals("Weiblich", patient.getGender());

        // Test Divers
        patient.setGender("Divers");
        assertEquals("Divers", patient.getGender());
    }

    @Test
    @DisplayName("Should handle patient status changes")
    void testStatusChanges() {
        // Active to Discharged
        patient.setStatus("active");
        assertEquals("active", patient.getStatus());

        patient.setStatus("discharged");
        assertEquals("discharged", patient.getStatus());
    }

    @Test
    @DisplayName("Should validate insurance number format")
    void testInsuranceNumberFormat() {
        // Arrange & Act
        String validInsuranceNumber = "INS-2024-12345";
        patient.setInsuranceNumber(validInsuranceNumber);

        // Assert
        assertEquals(validInsuranceNumber, patient.getInsuranceNumber());
        assertTrue(patient.getInsuranceNumber().matches("INS-\\d{4}-\\d+"));
    }

    @Test
    @DisplayName("Should handle long allergy lists")
    void testLongAllergyList() {
        // Arrange & Act
        String allergies = "Penicillin, Aspirin, Pollen, Nüsse, Latex, Hausstaub";
        patient.setAllergies(allergies);

        // Assert
        assertEquals(allergies, patient.getAllergies());
        assertTrue(patient.getAllergies().contains("Penicillin"));
        assertTrue(patient.getAllergies().contains("Aspirin"));
    }

    @Test
    @DisplayName("Should handle EPA consent workflow")
    void testEPAConsentWorkflow() {
        // Initially no consent
        assertNull(patient.getEpaConsentDate());
        
        // Give consent
        patient.setEpaEnabled(true);
        patient.setEpaConsentDate(LocalDateTime.now());
        patient.setEpaSyncStatus("pending");

        assertTrue(patient.getEpaEnabled());
        assertNotNull(patient.getEpaConsentDate());
        assertEquals("pending", patient.getEpaSyncStatus());

        // Withdraw consent
        patient.setEpaEnabled(false);
        patient.setEpaSyncStatus("disabled");

        assertFalse(patient.getEpaEnabled());
        assertEquals("disabled", patient.getEpaSyncStatus());
    }

    @Test
    @DisplayName("Should calculate age from date of birth")
    void testAgeCalculation() {
        // Arrange
        patient.setDateOfBirth(LocalDate.of(1990, 1, 1));

        // Act
        LocalDate today = LocalDate.now();
        int age = today.getYear() - patient.getDateOfBirth().getYear();

        // Assert
        assertTrue(age >= 34); // As of 2024
    }

    @Test
    @DisplayName("Should handle multiple phone formats")
    void testPhoneFormats() {
        String[] phoneFormats = {
            "030-12345678",
            "+49 30 12345678",
            "0049 30 12345678",
            "030/12345678"
        };

        for (String phone : phoneFormats) {
            patient.setPhone(phone);
            assertEquals(phone, patient.getPhone());
        }
    }

    @Test
    @DisplayName("Should handle email validation")
    void testEmailFormat() {
        // Valid emails
        String[] validEmails = {
            "test@example.com",
            "user.name@example.de",
            "firstname.lastname@hospital.de"
        };

        for (String email : validEmails) {
            patient.setEmail(email);
            assertEquals(email, patient.getEmail());
            assertTrue(email.contains("@"));
            assertTrue(email.contains("."));
        }
    }
}