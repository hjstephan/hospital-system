package com.hospital.rest;

import com.hospital.entity.Patient;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
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
 * Unit tests for PatientResource REST API
 */
class PatientResourceTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Patient> typedQuery;

    @InjectMocks
    private PatientResource patientResource;

    private Patient testPatient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Create test patient
        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setFirstName("Max");
        testPatient.setLastName("Mustermann");
        testPatient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        testPatient.setGender("Männlich");
        testPatient.setInsuranceNumber("INS-2024-001");
        testPatient.setBloodType("A+");
        testPatient.setStatus("active");
        testPatient.setEpaEnabled(true);
    }

    @Test
    @DisplayName("Should get all patients successfully")
    void testGetAllPatients() {
        // Arrange
        List<Patient> patients = Arrays.asList(testPatient);
        when(entityManager.createNamedQuery("Patient.findAll", Patient.class))
            .thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(patients);

        // Act
        Response response = patientResource.getAllPatients(null);

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        List<Patient> result = (List<Patient>) response.getEntity();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Max", result.get(0).getFirstName());
    }

    @Test
    @DisplayName("Should get active patients only")
    void testGetActivePatients() {
        // Arrange
        List<Patient> activePatients = Arrays.asList(testPatient);
        when(entityManager.createNamedQuery("Patient.findActive", Patient.class))
            .thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(activePatients);

        // Act
        Response response = patientResource.getAllPatients("active");

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        List<Patient> result = (List<Patient>) response.getEntity();
        assertEquals(1, result.size());
        assertEquals("active", result.get(0).getStatus());
    }

    @Test
    @DisplayName("Should get patient by ID")
    void testGetPatientById() {
        // Arrange
        when(entityManager.find(Patient.class, 1L)).thenReturn(testPatient);

        // Act
        Response response = patientResource.getPatient(1L);

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Patient result = (Patient) response.getEntity();
        assertNotNull(result);
        assertEquals("Max", result.getFirstName());
        assertEquals("Mustermann", result.getLastName());
    }

    @Test
    @DisplayName("Should return 404 when patient not found")
    void testGetPatientNotFound() {
        // Arrange
        when(entityManager.find(Patient.class, 999L)).thenReturn(null);

        // Act
        Response response = patientResource.getPatient(999L);

        // Assert
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    @DisplayName("Should search patients by name")
    void testSearchPatients() {
        // Arrange
        List<Patient> searchResults = Arrays.asList(testPatient);
        when(entityManager.createNamedQuery("Patient.searchByName", Patient.class))
            .thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("search"), anyString())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(searchResults);

        // Act
        Response response = patientResource.searchPatients("Max");

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        List<Patient> result = (List<Patient>) response.getEntity();
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should create new patient successfully")
    void testCreatePatient() {
        // Arrange
        Patient newPatient = new Patient();
        newPatient.setFirstName("Anna");
        newPatient.setLastName("Schmidt");
        newPatient.setDateOfBirth(LocalDate.of(1995, 5, 15));
        newPatient.setGender("Weiblich");
        newPatient.setInsuranceNumber("INS-2024-002");

        doNothing().when(entityManager).persist(any(Patient.class));
        doNothing().when(entityManager).flush();

        // Act
        Response response = patientResource.createPatient(newPatient);

        // Assert
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        verify(entityManager, times(1)).persist(any(Patient.class));
        verify(entityManager, times(1)).flush();
    }

    @Test
    @DisplayName("Should update existing patient")
    void testUpdatePatient() {
        // Arrange
        Patient updatedData = new Patient();
        updatedData.setFirstName("Maximilian");
        updatedData.setLastName("Mustermann");
        updatedData.setDateOfBirth(LocalDate.of(1990, 1, 1));
        updatedData.setGender("Männlich");
        updatedData.setInsuranceNumber("INS-2024-001");
        updatedData.setPhone("0123456789");
        updatedData.setEmail("max@example.com");

        when(entityManager.find(Patient.class, 1L)).thenReturn(testPatient);
        when(entityManager.merge(any(Patient.class))).thenReturn(testPatient);
        doNothing().when(entityManager).flush();

        // Act
        Response response = patientResource.updatePatient(1L, updatedData);

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(entityManager, times(1)).merge(any(Patient.class));
        verify(entityManager, times(1)).flush();
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent patient")
    void testUpdatePatientNotFound() {
        // Arrange
        Patient updatedData = new Patient();
        when(entityManager.find(Patient.class, 999L)).thenReturn(null);

        // Act
        Response response = patientResource.updatePatient(999L, updatedData);

        // Assert
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        verify(entityManager, never()).merge(any(Patient.class));
    }

    @Test
    @DisplayName("Should delete patient successfully")
    void testDeletePatient() {
        // Arrange
        when(entityManager.find(Patient.class, 1L)).thenReturn(testPatient);
        doNothing().when(entityManager).remove(any(Patient.class));

        // Act
        Response response = patientResource.deletePatient(1L);

        // Assert
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        verify(entityManager, times(1)).remove(testPatient);
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent patient")
    void testDeletePatientNotFound() {
        // Arrange
        when(entityManager.find(Patient.class, 999L)).thenReturn(null);

        // Act
        Response response = patientResource.deletePatient(999L);

        // Assert
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        verify(entityManager, never()).remove(any(Patient.class));
    }

    @Test
    @DisplayName("Should handle database exceptions gracefully")
    void testHandleDatabaseException() {
        // Arrange
        when(entityManager.createNamedQuery("Patient.findAll", Patient.class))
            .thenThrow(new RuntimeException("Database error"));

        // Act
        Response response = patientResource.getAllPatients(null);

        // Assert
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }
}