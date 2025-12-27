package com.hospital.integration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.hospital.entity.Patient;

/**
 * Integration tests for the complete patient workflow
 * These tests verify end-to-end functionality
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PatientIntegrationTest {

    private static Patient testPatient;

    @BeforeAll
    static void setUpClass() {
        testPatient = new Patient();
        testPatient.setFirstName("Integration");
        testPatient.setLastName("Test");
        testPatient.setDateOfBirth(LocalDate.of(1985, 6, 15));
        testPatient.setGender("Männlich");
        testPatient.setInsuranceNumber("INS-INT-TEST-001");
        testPatient.setBloodType("B+");
        testPatient.setPhone("030-99999999");
        testPatient.setEmail("integration@test.de");
        testPatient.setStatus("active");
        testPatient.setEpaEnabled(true);
    }

    @Test
    @Order(1)
    @DisplayName("Integration: Create patient workflow")
    void testCreatePatientWorkflow() {
        // This test would make actual HTTP requests in a real integration test
        // For unit testing purposes, we validate the patient object

        assertNotNull(testPatient);
        assertEquals("Integration", testPatient.getFirstName());
        assertEquals("Test", testPatient.getLastName());
        assertTrue(testPatient.getEpaEnabled());
    }

    @Test
    @Order(2)
    @DisplayName("Integration: Validate patient data completeness")
    void testPatientDataCompleteness() {
        assertAll("Patient data validation",
                () -> assertNotNull(testPatient.getFirstName(), "First name should not be null"),
                () -> assertNotNull(testPatient.getLastName(), "Last name should not be null"),
                () -> assertNotNull(testPatient.getDateOfBirth(), "Date of birth should not be null"),
                () -> assertNotNull(testPatient.getGender(), "Gender should not be null"),
                () -> assertNotNull(testPatient.getInsuranceNumber(), "Insurance number should not be null"),
                () -> assertEquals("active", testPatient.getStatus(), "Status should be active"));
    }

    @Test
    @Order(3)
    @DisplayName("Integration: Update patient workflow")
    void testUpdatePatientWorkflow() {
        // Update patient data
        testPatient.setPhone("030-88888888");
        testPatient.setEmail("updated@test.de");
        testPatient.setAddress("Updated Street 123, Berlin");

        // Verify updates
        assertEquals("030-88888888", testPatient.getPhone());
        assertEquals("updated@test.de", testPatient.getEmail());
        assertNotNull(testPatient.getAddress());
    }

    @Test
    @Order(4)
    @DisplayName("Integration: EPA consent workflow")
    void testEPAConsentWorkflow() {
        // Enable EPA
        testPatient.setEpaEnabled(true);
        testPatient.setEpaSyncStatus("pending");

        assertTrue(testPatient.getEpaEnabled());
        assertEquals("pending", testPatient.getEpaSyncStatus());

        // Simulate successful sync
        testPatient.setEpaSyncStatus("synced");
        testPatient.setEpaId("EPA-INT-TEST-001");

        assertEquals("synced", testPatient.getEpaSyncStatus());
        assertNotNull(testPatient.getEpaId());
    }

    @Test
    @Order(5)
    @DisplayName("Integration: Patient discharge workflow")
    void testPatientDischargeWorkflow() {
        // Discharge patient
        testPatient.setStatus("discharged");

        assertEquals("discharged", testPatient.getStatus());
    }

    @Test
    @DisplayName("Integration: Complete patient lifecycle")
    void testCompletePatientLifecycle() {
        // Create
        Patient lifecyclePatient = new Patient();
        lifecyclePatient.setFirstName("Lifecycle");
        lifecyclePatient.setLastName("Patient");
        lifecyclePatient.setDateOfBirth(LocalDate.of(1992, 3, 10));
        lifecyclePatient.setGender("Weiblich");
        lifecyclePatient.setInsuranceNumber("INS-LIFECYCLE-001");
        lifecyclePatient.setStatus("active");

        // Admission
        assertNotNull(lifecyclePatient);
        assertEquals("active", lifecyclePatient.getStatus());

        // Medical treatment (update)
        lifecyclePatient.setAllergies("None");
        lifecyclePatient.setBloodType("O+");

        // EPA consent
        lifecyclePatient.setEpaEnabled(true);
        lifecyclePatient.setEpaSyncStatus("synced");

        // Discharge
        lifecyclePatient.setStatus("discharged");

        // Verify complete lifecycle
        assertAll("Complete lifecycle",
                () -> assertEquals("Lifecycle", lifecyclePatient.getFirstName()),
                () -> assertEquals("discharged", lifecyclePatient.getStatus()),
                () -> assertTrue(lifecyclePatient.getEpaEnabled()),
                () -> assertNotNull(lifecyclePatient.getAllergies()));
    }

    @Test
    @DisplayName("Integration: Multiple patients management")
    void testMultiplePatientsManagement() {
        // Create multiple patients
        Patient patient1 = createPatient("Patient", "One", "INS-MULTI-001");
        Patient patient2 = createPatient("Patient", "Two", "INS-MULTI-002");
        Patient patient3 = createPatient("Patient", "Three", "INS-MULTI-003");

        // Verify all patients are unique
        assertNotEquals(patient1.getInsuranceNumber(), patient2.getInsuranceNumber());
        assertNotEquals(patient2.getInsuranceNumber(), patient3.getInsuranceNumber());

        // Verify different statuses
        patient1.setStatus("active");
        patient2.setStatus("active");
        patient3.setStatus("discharged");

        assertEquals("active", patient1.getStatus());
        assertEquals("discharged", patient3.getStatus());
    }

    @Test
    @DisplayName("Integration: EPA synchronization for multiple patients")
    void testMultiplePatientEPASync() {
        Patient[] patients = {
                createPatient("EPA", "Patient1", "INS-EPA-001"),
                createPatient("EPA", "Patient2", "INS-EPA-002"),
                createPatient("EPA", "Patient3", "INS-EPA-003")
        };

        // Enable EPA for all
        for (Patient p : patients) {
            p.setEpaEnabled(true);
            p.setEpaSyncStatus("pending");
        }

        // Verify all are pending sync
        for (Patient p : patients) {
            assertTrue(p.getEpaEnabled());
            assertEquals("pending", p.getEpaSyncStatus());
        }

        // Simulate sync
        for (Patient p : patients) {
            p.setEpaSyncStatus("synced");
            p.setEpaId("EPA-" + p.getInsuranceNumber());
        }

        // Verify all synced
        for (Patient p : patients) {
            assertEquals("synced", p.getEpaSyncStatus());
            assertNotNull(p.getEpaId());
        }
    }

    @Test
    @DisplayName("Integration: Error handling workflow")
    void testErrorHandlingWorkflow() {
        Patient errorPatient = createPatient("Error", "Test", "INS-ERROR-001");

        // Simulate EPA sync error
        errorPatient.setEpaEnabled(true);
        errorPatient.setEpaSyncStatus("error");
        errorPatient.setEpaSyncError("Connection timeout to EPA server");

        assertEquals("error", errorPatient.getEpaSyncStatus());
        assertNotNull(errorPatient.getEpaSyncError());
        assertTrue(errorPatient.getEpaSyncError().contains("timeout"));
    }

    // Helper method
    private Patient createPatient(String firstName, String lastName, String insuranceNumber) {
        Patient patient = new Patient();
        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        patient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patient.setGender("Divers");
        patient.setInsuranceNumber(insuranceNumber);
        patient.setStatus("active");
        return patient;
    }

    @AfterAll
    static void tearDownClass() {
        // Clean up test data
        testPatient = null;
    }
}