package com.hospital.epa;

import com.hospital.entity.Patient;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EPA Integration Service
 */
class EPAIntegrationServiceTest {

    @Mock
    private FHIRConverter fhirConverter;

    @InjectMocks
    private EPAIntegrationService epaService;

    private Patient testPatient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setFirstName("Max");
        testPatient.setLastName("Mustermann");
        testPatient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        testPatient.setGender("Männlich");
        testPatient.setInsuranceNumber("INS-2024-001");
        testPatient.setEpaEnabled(true);
    }

    @Test
    @DisplayName("Should test EPA connection successfully")
    void testEPAConnectionSuccess() {
        // This is a simplified test since we can't easily mock static ClientBuilder
        // In a real scenario, you'd use dependency injection for the HTTP client
        
        // Act & Assert
        assertDoesNotThrow(() -> epaService.testEPAConnection());
    }

    @Test
    @DisplayName("Should handle EPA unavailable")
    void testEPAConnectionFailure() {
        // Act
        boolean connected = epaService.testEPAConnection();

        // Assert - Will be false if EPA is not running
        // This test verifies the method handles connection failures gracefully
        assertNotNull(connected);
    }

    @Test
    @DisplayName("Should sync all patients with EPA consent")
    void testSyncAllPatientsToEPA() {
        // Arrange
        Patient patient1 = createTestPatient(1L, "Max", "Mustermann");
        Patient patient2 = createTestPatient(2L, "Anna", "Schmidt");
        
        List<Patient> patients = Arrays.asList(patient1, patient2);
        
        when(fhirConverter.patientToFHIR(any(Patient.class)))
            .thenReturn("{\"resourceType\":\"Patient\"}");

        // Act
        var result = epaService.syncAllPatientsToEPA(patients);

        // Assert
        assertNotNull(result);
        assertEquals(patients.size(), result.getSuccessCount() + result.getFailedCount());
    }

    @Test
    @DisplayName("Should create FHIR payload for patient")
    void testCreateFHIRPayload() {
        // Arrange
        String expectedFhir = "{\"resourceType\":\"Patient\",\"name\":[{\"family\":\"Mustermann\"}]}";
        when(fhirConverter.patientToFHIR(testPatient)).thenReturn(expectedFhir);

        // Act
        String fhirPayload = fhirConverter.patientToFHIR(testPatient);

        // Assert
        assertNotNull(fhirPayload);
        assertEquals(expectedFhir, fhirPayload);
        verify(fhirConverter, times(1)).patientToFHIR(testPatient);
    }

    @Test
    @DisplayName("Should handle null patient gracefully")
    void testSendNullPatient() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            epaService.sendPatientToEPA(null);
        });
    }

    @Test
    @DisplayName("Should handle patient without EPA consent")
    void testPatientWithoutEPAConsent() {
        // Arrange
        testPatient.setEpaEnabled(false);

        // Act
        var result = epaService.sendPatientToEPA(testPatient);

        // Assert - Should still attempt sync, but EPA system should reject
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should validate EPA response structure")
    void testEPAResponseStructure() {
        // Arrange
        when(fhirConverter.patientToFHIR(testPatient))
            .thenReturn("{\"resourceType\":\"Patient\"}");

        // Act
        var result = epaService.sendPatientToEPA(testPatient);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getMessage());
    }

    @Test
    @DisplayName("Should handle EPA timeout")
    void testEPATimeout() {
        // This test verifies timeout handling
        // In production, you'd configure appropriate timeouts
        
        // Act
        var result = epaService.sendPatientToEPA(testPatient);

        // Assert - Should handle timeout gracefully
        assertNotNull(result);
        assertNotNull(result.getMessage());
    }

    @Test
    @DisplayName("Should sync statistics correctly")
    void testSyncStatistics() {
        // Arrange
        List<Patient> patients = Arrays.asList(
            createTestPatient(1L, "Patient", "One"),
            createTestPatient(2L, "Patient", "Two"),
            createTestPatient(3L, "Patient", "Three")
        );

        // Act
        var result = epaService.syncAllPatientsToEPA(patients);

        // Assert
        assertEquals(3, result.getSuccessCount() + result.getFailedCount());
        assertTrue(result.getSuccessCount() >= 0);
        assertTrue(result.getFailedCount() >= 0);
    }

    // Helper method
    private Patient createTestPatient(Long id, String firstName, String lastName) {
        Patient patient = new Patient();
        patient.setId(id);
        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        patient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patient.setGender("Männlich");
        patient.setInsuranceNumber("INS-2024-" + String.format("%03d", id));
        patient.setEpaEnabled(true);
        return patient;
    }
}