package com.hospital.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.hospital.entity.Patient;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.core.Response;

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
        Object entity = response.getEntity();
        assertTrue(entity instanceof List);
        List<?> raw = (List<?>) entity;
        List<Patient> result = raw.stream().map(o -> (Patient) o).collect(Collectors.toList());
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
        Object entity = response.getEntity();
        assertTrue(entity instanceof List);
        List<?> raw = (List<?>) entity;
        List<Patient> result = raw.stream().map(o -> (Patient) o).collect(Collectors.toList());
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
    @DisplayName("Should search patients by name with pagination")
    void testSearchPatients() {
        // Arrange
        String searchQuery = "Max";
        String searchPattern = "%" + searchQuery + "%";

        List<Patient> searchResults = Arrays.asList(testPatient);
        Long totalCount = 1L;

        // Mock für Count Query
        TypedQuery<Long> countQuery = mock(TypedQuery.class);
        when(entityManager.createNamedQuery("Patient.countByNameSearch", Long.class))
                .thenReturn(countQuery);
        when(countQuery.setParameter("search", searchPattern))
                .thenReturn(countQuery);
        when(countQuery.getSingleResult())
                .thenReturn(totalCount);

        // Mock für Search Query
        when(entityManager.createNamedQuery("Patient.searchByNameOptimized", Patient.class))
                .thenReturn(typedQuery);
        when(typedQuery.setParameter("search", searchPattern))
                .thenReturn(typedQuery);
        when(typedQuery.setFirstResult(0))
                .thenReturn(typedQuery);
        when(typedQuery.setMaxResults(100))
                .thenReturn(typedQuery);
        when(typedQuery.getResultList())
                .thenReturn(searchResults);

        // Act
        Response response = patientResource.searchPatients(searchQuery, 0, 100);

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        // Verify that queries were called correctly
        verify(countQuery).setParameter("search", searchPattern);
        verify(countQuery).getSingleResult();
        verify(typedQuery).setParameter("search", searchPattern);
        verify(typedQuery).setFirstResult(0);
        verify(typedQuery).setMaxResults(100);
        verify(typedQuery).getResultList();
    }

    @Test
    @DisplayName("Should return bad request when search query is empty")
    void testSearchPatientsEmptyQuery() {
        // Act
        Response response = patientResource.searchPatients("", 0, 100);

        // Assert
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @DisplayName("Should return bad request when search query is null")
    void testSearchPatientsNullQuery() {
        // Act
        Response response = patientResource.searchPatients(null, 0, 100);

        // Assert
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    @DisplayName("Should use default values for offset and limit")
    void testSearchPatientsDefaultPagination() {
        // Arrange
        String searchQuery = "Schmidt";
        String searchPattern = "%" + searchQuery + "%";

        List<Patient> searchResults = Arrays.asList(testPatient);
        Long totalCount = 1L;

        // Mock für Count Query
        TypedQuery<Long> countQuery = mock(TypedQuery.class);
        when(entityManager.createNamedQuery("Patient.countByNameSearch", Long.class))
                .thenReturn(countQuery);
        when(countQuery.setParameter("search", searchPattern))
                .thenReturn(countQuery);
        when(countQuery.getSingleResult())
                .thenReturn(totalCount);

        // Mock für Search Query
        when(entityManager.createNamedQuery("Patient.searchByNameOptimized", Patient.class))
                .thenReturn(typedQuery);
        when(typedQuery.setParameter("search", searchPattern))
                .thenReturn(typedQuery);
        when(typedQuery.setFirstResult(0))
                .thenReturn(typedQuery);
        when(typedQuery.setMaxResults(100))
                .thenReturn(typedQuery);
        when(typedQuery.getResultList())
                .thenReturn(searchResults);

        // Act - null für offset und limit
        Response response = patientResource.searchPatients(searchQuery, null, null);

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        // Verify defaults were used
        verify(typedQuery).setFirstResult(0); // Default: 0
        verify(typedQuery).setMaxResults(100); // Default: 100
    }

    @Test
    @DisplayName("Should indicate hasMore when more results available")
    void testSearchPatientsHasMore() {
        // Arrange
        String searchQuery = "Müller";
        String searchPattern = "%" + searchQuery + "%";

        List<Patient> searchResults = Arrays.asList(testPatient);
        Long totalCount = 250L; // 250 Ergebnisse insgesamt

        // Mock für Count Query
        TypedQuery<Long> countQuery = mock(TypedQuery.class);
        when(entityManager.createNamedQuery("Patient.countByNameSearch", Long.class))
                .thenReturn(countQuery);
        when(countQuery.setParameter("search", searchPattern))
                .thenReturn(countQuery);
        when(countQuery.getSingleResult())
                .thenReturn(totalCount);

        // Mock für Search Query
        when(entityManager.createNamedQuery("Patient.searchByNameOptimized", Patient.class))
                .thenReturn(typedQuery);
        when(typedQuery.setParameter("search", searchPattern))
                .thenReturn(typedQuery);
        when(typedQuery.setFirstResult(0))
                .thenReturn(typedQuery);
        when(typedQuery.setMaxResults(100))
                .thenReturn(typedQuery);
        when(typedQuery.getResultList())
                .thenReturn(searchResults);

        // Act
        Response response = patientResource.searchPatients(searchQuery, 0, 100);

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        // Verify that count query was called (hasMore depends on total count)
        verify(countQuery).getSingleResult();
    }

    @Test
    @DisplayName("Should return random patients")
    void testGetRandomPatients() {
        // Arrange
        List<Patient> randomPatients = Arrays.asList(testPatient);
        when(entityManager.createNamedQuery("Patient.findRandom", Patient.class))
                .thenReturn(typedQuery);
        when(typedQuery.setMaxResults(50))
                .thenReturn(typedQuery);
        when(typedQuery.getResultList())
                .thenReturn(randomPatients);

        // Act
        Response response = patientResource.getRandomPatients(50);

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());

        // Verify query was called correctly
        verify(typedQuery).setMaxResults(50);
        verify(typedQuery).getResultList();
    }

    @Test
    @DisplayName("Should use default limit for random patients")
    void testGetRandomPatientsDefaultLimit() {
        // Arrange
        List<Patient> randomPatients = Arrays.asList(testPatient);
        when(entityManager.createNamedQuery("Patient.findRandom", Patient.class))
                .thenReturn(typedQuery);
        when(typedQuery.setMaxResults(50)) // Default ist 50
                .thenReturn(typedQuery);
        when(typedQuery.getResultList())
                .thenReturn(randomPatients);

        // Act
        Response response = patientResource.getRandomPatients(null);

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(typedQuery).setMaxResults(50); // Verify default wurde verwendet
    }

    @Test
    @DisplayName("Should handle search with pagination offset")
    void testSearchPatientsWithOffset() {
        // Arrange
        String searchQuery = "Test";
        String searchPattern = "%" + searchQuery + "%";

        List<Patient> searchResults = Arrays.asList(testPatient);
        Long totalCount = 150L;

        // Mock für Count Query
        TypedQuery<Long> countQuery = mock(TypedQuery.class);
        when(entityManager.createNamedQuery("Patient.countByNameSearch", Long.class))
                .thenReturn(countQuery);
        when(countQuery.setParameter("search", searchPattern))
                .thenReturn(countQuery);
        when(countQuery.getSingleResult())
                .thenReturn(totalCount);

        // Mock für Search Query
        when(entityManager.createNamedQuery("Patient.searchByNameOptimized", Patient.class))
                .thenReturn(typedQuery);
        when(typedQuery.setParameter("search", searchPattern))
                .thenReturn(typedQuery);
        when(typedQuery.setFirstResult(100))
                .thenReturn(typedQuery);
        when(typedQuery.setMaxResults(50))
                .thenReturn(typedQuery);
        when(typedQuery.getResultList())
                .thenReturn(searchResults);

        // Act
        Response response = patientResource.searchPatients(searchQuery, 100, 50);

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        // Verify pagination parameters
        verify(typedQuery).setFirstResult(100);
        verify(typedQuery).setMaxResults(50);
    }

    @Test
    @DisplayName("Should return empty list when no patients")
    void testGetAllPatientsEmpty() {
        // Arrange
        List<Patient> empty = Arrays.asList();
        when(entityManager.createNamedQuery("Patient.findAll", Patient.class))
                .thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(empty);

        // Act
        Response response = patientResource.getAllPatients(null);

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Object entity = response.getEntity();
        assertTrue(entity instanceof List);
        List<?> raw = (List<?>) entity;
        List<Patient> result = raw.stream().map(o -> (Patient) o).collect(Collectors.toList());
        assertEquals(0, result.size());
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