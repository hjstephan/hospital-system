package com.hospital.epa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hospital.entity.Patient;

import jakarta.json.Json;
import jakarta.json.JsonObject;

class FHIRConverterMoreTest {

    private FHIRConverter converter;
    private Patient patient;

    @BeforeEach
    void setUp() {
        converter = new FHIRConverter();
        patient = new Patient();
        patient.setId(42L);
        patient.setFirstName("Eva");
        patient.setLastName("Musterfrau");
        patient.setDateOfBirth(LocalDate.of(1985, 3, 20));
        patient.setGender("Weiblich");
        patient.setPhone("+491231234567");
        patient.setEmail("eva@example.com");
        patient.setAddress("Musterstraße 1, 12345 Berlin");
        patient.setInsuranceNumber("INS-1985-042");
        patient.setBloodType("O+");
        patient.setAllergies("Penicillin");
        patient.setEmergencyContactName("Hans Muster");
        patient.setEmergencyContactPhone("+491239876543");
        patient.setStatus("active");
    }

    @Test
    @DisplayName("patientToFHIR includes expected fields and extensions")
    void testPatientToFHIRContainsFields() {
        String json = converter.patientToFHIR(patient);
        assertNotNull(json);
        // Basic checks for resource type and name
        assertTrue(json.contains("\"resourceType\":\"Patient\""));
        assertTrue(json.contains("Musterfrau"));
        assertTrue(json.contains("Eva"));
        // phone and email should be present
        assertTrue(json.contains("+491231234567"));
        assertTrue(json.contains("eva@example.com"));
        // address
        assertTrue(json.contains("Musterstra\u00dfe") || json.contains("Musterstraße") || json.contains("Musterstra"));
        // extensions for blood type and allergies
        assertTrue(json.contains("blood-type") || json.contains("blood-type"));
        assertTrue(json.contains("allergies") || json.contains("allergies"));
    }

    @Test
    @DisplayName("fhirToPatient parses basic FHIR object back into Patient")
    void testFhirToPatientRoundtrip() {
        String json = converter.patientToFHIR(patient);
        JsonObject obj = Json.createReader(new java.io.StringReader(json)).readObject();

        Patient back = converter.fhirToPatient(obj);
        assertNotNull(back);
        assertEquals("Musterfrau", back.getLastName());
        assertEquals("Eva", back.getFirstName());
        assertEquals("INS-1985-042", back.getInsuranceNumber());
        assertEquals("active", back.getStatus());
    }

    @Test
    @DisplayName("createPatientBundle produces a bundle with correct total and entries")
    void testCreatePatientBundle() {
        java.util.List<Patient> patients = java.util.Arrays.asList(patient, patient);
        String bundleJson = converter.createPatientBundle(patients);
        assertNotNull(bundleJson);
        assertTrue(bundleJson.contains("\"resourceType\":\"Bundle\""));
        assertTrue(bundleJson.contains("\"total\": 2") || bundleJson.contains("\"total\":2"));
    }
}
