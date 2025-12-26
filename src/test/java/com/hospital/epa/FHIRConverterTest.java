package com.hospital.epa;

import com.hospital.entity.Patient;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.StringReader;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FHIR converter
 */
class FHIRConverterTest {

    private FHIRConverter fhirConverter;
    private Patient testPatient;

    @BeforeEach
    void setUp() {
        fhirConverter = new FHIRConverter();
        
        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setFirstName("Max");
        testPatient.setLastName("Mustermann");
        testPatient.setDateOfBirth(LocalDate.of(1990, 5, 15));
        testPatient.setGender("Männlich");
        testPatient.setPhone("030-12345678");
        testPatient.setEmail("max@example.com");
        testPatient.setAddress("Hauptstraße 1, 10115 Berlin");
        testPatient.setInsuranceNumber("INS-2024-001");
        testPatient.setBloodType("A+");
        testPatient.setAllergies("Penicillin");
        testPatient.setEmergencyContactName("Anna Mustermann");
        testPatient.setEmergencyContactPhone("030-87654321");
        testPatient.setStatus("active");
    }

    @Test
    @DisplayName("Should convert patient to FHIR format")
    void testPatientToFHIR() {
        // Act
        String fhirJson = fhirConverter.patientToFHIR(testPatient);

        // Assert
        assertNotNull(fhirJson);
        assertFalse(fhirJson.isEmpty());
        
        // Parse JSON
        JsonReader reader = Json.createReader(new StringReader(fhirJson));
        JsonObject fhirPatient = reader.readObject();
        
        assertEquals("Patient", fhirPatient.getString("resourceType"));
        assertEquals("male", fhirPatient.getString("gender"));
        assertEquals("1990-05-15", fhirPatient.getString("birthDate"));
        assertTrue(fhirPatient.getBoolean("active"));
    }

    @Test
    @DisplayName("Should include identifier in FHIR format")
    void testFHIRIdentifier() {
        // Act
        String fhirJson = fhirConverter.patientToFHIR(testPatient);
        JsonReader reader = Json.createReader(new StringReader(fhirJson));
        JsonObject fhirPatient = reader.readObject();

        // Assert
        assertTrue(fhirPatient.containsKey("identifier"));
        JsonObject identifier = fhirPatient.getJsonArray("identifier").getJsonObject(0);
        assertEquals("official", identifier.getString("use"));
        assertEquals("INS-2024-001", identifier.getString("value"));
    }

    @Test
    @DisplayName("Should include name in FHIR format")
    void testFHIRName() {
        // Act
        String fhirJson = fhirConverter.patientToFHIR(testPatient);
        JsonReader reader = Json.createReader(new StringReader(fhirJson));
        JsonObject fhirPatient = reader.readObject();

        // Assert
        assertTrue(fhirPatient.containsKey("name"));
        JsonObject name = fhirPatient.getJsonArray("name").getJsonObject(0);
        assertEquals("official", name.getString("use"));
        assertEquals("Mustermann", name.getString("family"));
        assertEquals("Max", name.getJsonArray("given").getString(0));
    }

    @Test
    @DisplayName("Should include telecom in FHIR format")
    void testFHIRTelecom() {
        // Act
        String fhirJson = fhirConverter.patientToFHIR(testPatient);
        JsonReader reader = Json.createReader(new StringReader(fhirJson));
        JsonObject fhirPatient = reader.readObject();

        // Assert
        assertTrue(fhirPatient.containsKey("telecom"));
        assertEquals(2, fhirPatient.getJsonArray("telecom").size());
        
        JsonObject phone = fhirPatient.getJsonArray("telecom").getJsonObject(0);
        assertEquals("phone", phone.getString("system"));
        assertEquals("030-12345678", phone.getString("value"));
        
        JsonObject email = fhirPatient.getJsonArray("telecom").getJsonObject(1);
        assertEquals("email", email.getString("system"));
        assertEquals("max@example.com", email.getString("value"));
    }

    @Test
    @DisplayName("Should include address in FHIR format")
    void testFHIRAddress() {
        // Act
        String fhirJson = fhirConverter.patientToFHIR(testPatient);
        JsonReader reader = Json.createReader(new StringReader(fhirJson));
        JsonObject fhirPatient = reader.readObject();

        // Assert
        assertTrue(fhirPatient.containsKey("address"));
        JsonObject address = fhirPatient.getJsonArray("address").getJsonObject(0);
        assertEquals("home", address.getString("use"));
        assertEquals("physical", address.getString("type"));
        assertEquals("Hauptstraße 1, 10115 Berlin", address.getString("text"));
    }

    @Test
    @DisplayName("Should include extensions in FHIR format")
    void testFHIRExtensions() {
        // Act
        String fhirJson = fhirConverter.patientToFHIR(testPatient);
        JsonReader reader = Json.createReader(new StringReader(fhirJson));
        JsonObject fhirPatient = reader.readObject();

        // Assert
        assertTrue(fhirPatient.containsKey("extension"));
        assertEquals(2, fhirPatient.getJsonArray("extension").size());
        
        // Blood type extension
        JsonObject bloodTypeExt = fhirPatient.getJsonArray("extension").getJsonObject(0);
        assertTrue(bloodTypeExt.getString("url").contains("blood-type"));
        assertEquals("A+", bloodTypeExt.getString("valueString"));
        
        // Allergies extension
        JsonObject allergiesExt = fhirPatient.getJsonArray("extension").getJsonObject(1);
        assertTrue(allergiesExt.getString("url").contains("allergies"));
        assertEquals("Penicillin", allergiesExt.getString("valueString"));
    }

    @Test
    @DisplayName("Should include emergency contact in FHIR format")
    void testFHIREmergencyContact() {
        // Act
        String fhirJson = fhirConverter.patientToFHIR(testPatient);
        JsonReader reader = Json.createReader(new StringReader(fhirJson));
        JsonObject fhirPatient = reader.readObject();

        // Assert
        assertTrue(fhirPatient.containsKey("contact"));
        JsonObject contact = fhirPatient.getJsonArray("contact").getJsonObject(0);
        
        JsonObject contactName = contact.getJsonObject("name");
        assertEquals("Anna Mustermann", contactName.getString("text"));
        
        JsonObject contactPhone = contact.getJsonArray("telecom").getJsonObject(0);
        assertEquals("phone", contactPhone.getString("system"));
        assertEquals("030-87654321", contactPhone.getString("value"));
    }

    @Test
    @DisplayName("Should convert gender correctly")
    void testGenderConversion() {
        // Test Männlich -> male
        testPatient.setGender("Männlich");
        String fhirJson = fhirConverter.patientToFHIR(testPatient);
        JsonObject fhirPatient = Json.createReader(new StringReader(fhirJson)).readObject();
        assertEquals("male", fhirPatient.getString("gender"));

        // Test Weiblich -> female
        testPatient.setGender("Weiblich");
        fhirJson = fhirConverter.patientToFHIR(testPatient);
        fhirPatient = Json.createReader(new StringReader(fhirJson)).readObject();
        assertEquals("female", fhirPatient.getString("gender"));

        // Test Divers -> other
        testPatient.setGender("Divers");
        fhirJson = fhirConverter.patientToFHIR(testPatient);
        fhirPatient = Json.createReader(new StringReader(fhirJson)).readObject();
        assertEquals("other", fhirPatient.getString("gender"));
    }

    @Test
    @DisplayName("Should handle patient with minimal data")
    void testMinimalPatientToFHIR() {
        // Arrange
        Patient minimalPatient = new Patient();
        minimalPatient.setFirstName("Test");
        minimalPatient.setLastName("User");
        minimalPatient.setDateOfBirth(LocalDate.of(2000, 1, 1));
        minimalPatient.setGender("Divers");
        minimalPatient.setInsuranceNumber("INS-MIN-001");

        // Act
        String fhirJson = fhirConverter.patientToFHIR(minimalPatient);
        JsonObject fhirPatient = Json.createReader(new StringReader(fhirJson)).readObject();

        // Assert
        assertNotNull(fhirJson);
        assertEquals("Patient", fhirPatient.getString("resourceType"));
        assertEquals("Test", fhirPatient.getJsonArray("name").getJsonObject(0)
            .getJsonArray("given").getString(0));
    }

    @Test
    @DisplayName("Should convert inactive patient correctly")
    void testInactivePatient() {
        // Arrange
        testPatient.setStatus("discharged");

        // Act
        String fhirJson = fhirConverter.patientToFHIR(testPatient);
        JsonObject fhirPatient = Json.createReader(new StringReader(fhirJson)).readObject();

        // Assert
        assertFalse(fhirPatient.getBoolean("active"));
    }

    @Test
    @DisplayName("Should convert FHIR back to Patient entity")
    void testFHIRToPatient() {
        // Arrange
        String fhirJson = fhirConverter.patientToFHIR(testPatient);
        JsonObject fhirPatient = Json.createReader(new StringReader(fhirJson)).readObject();

        // Act
        Patient convertedPatient = fhirConverter.fhirToPatient(fhirPatient);

        // Assert
        assertNotNull(convertedPatient);
        assertEquals("Max", convertedPatient.getFirstName());
        assertEquals("Mustermann", convertedPatient.getLastName());
        assertEquals(LocalDate.of(1990, 5, 15), convertedPatient.getDateOfBirth());
        assertEquals("Männlich", convertedPatient.getGender());
        assertEquals("INS-2024-001", convertedPatient.getInsuranceNumber());
        assertEquals("active", convertedPatient.getStatus());
    }

    @Test
    @DisplayName("Should create patient bundle")
    void testCreatePatientBundle() {
        // Arrange
        Patient patient2 = new Patient();
        patient2.setFirstName("Anna");
        patient2.setLastName("Schmidt");
        patient2.setDateOfBirth(LocalDate.of(1995, 3, 20));
        patient2.setGender("Weiblich");
        patient2.setInsuranceNumber("INS-2024-002");

        List<Patient> patients = Arrays.asList(testPatient, patient2);

        // Act
        String bundleJson = fhirConverter.createPatientBundle(patients);
        JsonObject bundle = Json.createReader(new StringReader(bundleJson)).readObject();

        // Assert
        assertNotNull(bundleJson);
        assertEquals("Bundle", bundle.getString("resourceType"));
        assertEquals("collection", bundle.getString("type"));
        assertEquals(2, bundle.getInt("total"));
        assertEquals(2, bundle.getJsonArray("entry").size());
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void testNullValueHandling() {
        // Arrange
        Patient patientWithNulls = new Patient();
        patientWithNulls.setFirstName("Test");
        patientWithNulls.setLastName("User");
        patientWithNulls.setDateOfBirth(LocalDate.of(2000, 1, 1));
        patientWithNulls.setGender("Männlich");
        patientWithNulls.setInsuranceNumber("INS-NULL-001");
        // All other fields are null

        // Act
        String fhirJson = fhirConverter.patientToFHIR(patientWithNulls);

        // Assert
        assertNotNull(fhirJson);
        assertFalse(fhirJson.contains("null"));
    }

    @Test
    @DisplayName("Should validate FHIR JSON structure")
    void testFHIRJSONStructure() {
        // Act
        String fhirJson = fhirConverter.patientToFHIR(testPatient);
        JsonObject fhirPatient = Json.createReader(new StringReader(fhirJson)).readObject();

        // Assert - Check all required FHIR Patient fields
        assertTrue(fhirPatient.containsKey("resourceType"));
        assertTrue(fhirPatient.containsKey("identifier"));
        assertTrue(fhirPatient.containsKey("name"));
        assertTrue(fhirPatient.containsKey("gender"));
        assertTrue(fhirPatient.containsKey("birthDate"));
        assertTrue(fhirPatient.containsKey("active"));
    }
}