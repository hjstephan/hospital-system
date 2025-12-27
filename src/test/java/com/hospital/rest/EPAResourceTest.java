package com.hospital.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.hospital.entity.Patient;
import com.hospital.epa.EPAIntegrationService;
import com.hospital.epa.EPAResponse;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.core.Response;

class EPAResourceTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Patient> typedQuery;

    @Mock
    private EPAIntegrationService epaService;

    @InjectMocks
    private EPAResource epaResource;

    private Patient patient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        patient = new Patient();
        patient.setId(10L);
        patient.setFirstName("Lena");
        patient.setLastName("Vorbild");
        patient.setDateOfBirth(LocalDate.of(1975, 6, 15));
        patient.setInsuranceNumber("INS-1975-010");
        patient.setEpaEnabled(true);
        patient.setStatus("active");
    }

    @Test
    @DisplayName("syncPatientToEPA returns 404 when patient not found")
    void testSyncPatientNotFound() {
        when(em.find(Patient.class, 999L)).thenReturn(null);
        Response resp = epaResource.syncPatientToEPA(999L);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), resp.getStatus());
    }

    @Test
    @DisplayName("syncPatientToEPA returns 403 when consent missing")
    void testSyncPatientConsentMissing() {
        patient.setEpaEnabled(false);
        when(em.find(Patient.class, 10L)).thenReturn(patient);
        Response resp = epaResource.syncPatientToEPA(10L);
        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), resp.getStatus());
    }

    @Test
    @DisplayName("syncPatientToEPA success path updates patient and returns OK")
    void testSyncPatientSuccess() {
        when(em.find(Patient.class, 10L)).thenReturn(patient);
        when(epaService.sendPatientToEPA(patient)).thenReturn(new EPAResponse(true, "EPA-123", "ok"));
        when(em.merge(any(Patient.class))).thenReturn(patient);
        doNothing().when(em).flush();

        Response resp = epaResource.syncPatientToEPA(10L);
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
    }

    @Test
    @DisplayName("syncAllPatientsToEPA returns message when none have consent")
    void testSyncAllNoConsent() {
        // create a list where nobody has consent
        Patient p1 = new Patient();
        p1.setId(1L);
        p1.setEpaEnabled(false);
        when(em.createNamedQuery("Patient.findActive", Patient.class)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList(p1));

        Response resp = epaResource.syncAllPatientsToEPA();
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        String body = (String) resp.getEntity();
        assertTrue(body.contains("Keine Patienten"));
    }

    @Test
    @DisplayName("setEPAConsent toggles consent and returns OK")
    void testSetEPAConsent() {
        when(em.find(Patient.class, 10L)).thenReturn(patient);
        when(em.merge(any(Patient.class))).thenReturn(patient);
        doNothing().when(em).flush();

        Response resp = epaResource.setEPAConsent(10L, true);
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
    }

    @Test
    @DisplayName("getEPAStatus returns formatted JSON with fields")
    void testGetEPAStatus() {
        patient.setEpaId("EPA-555");
        patient.setEpaSyncStatus("synced");
        patient.setEpaLastSync(LocalDateTime.now());
        when(em.find(Patient.class, 10L)).thenReturn(patient);

        Response resp = epaResource.getEPAStatus(10L);
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        String body = (String) resp.getEntity();
        assertTrue(body.contains("EPA-555") || body.contains("epaId"));
    }

    @Test
    @DisplayName("fetchFromEPA returns 404 when not found")
    void testFetchFromEPANotFound() {
        when(epaService.getPatientFromEPA("X"))
                .thenReturn(null);
        Response resp = epaResource.fetchFromEPA("X");
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), resp.getStatus());
    }

    @Test
    @DisplayName("testConnection forwards service result")
    void testConnection() {
        when(epaService.testEPAConnection()).thenReturn(true);
        Response resp = epaResource.testConnection();
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());

        when(epaService.testEPAConnection()).thenReturn(false);
        Response resp2 = epaResource.testConnection();
        assertEquals(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(), resp2.getStatus());
    }

    @Test
    @DisplayName("getEPAStatistics returns counts in JSON")
    void testGetEPAStatistics() {
        // Mock a simple TypedQuery<Long> and return it for all count queries
        TypedQuery<Long> countQuery = mock(TypedQuery.class);
        when(countQuery.getSingleResult()).thenReturn(3L);
        when(em.createQuery("SELECT COUNT(p) FROM Patient p", Long.class)).thenReturn(countQuery);
        when(em.createQuery("SELECT COUNT(p) FROM Patient p WHERE p.epaEnabled = true", Long.class))
                .thenReturn(countQuery);
        when(em.createQuery("SELECT COUNT(p) FROM Patient p WHERE p.epaSyncStatus = 'synced'", Long.class))
                .thenReturn(countQuery);
        when(em.createQuery("SELECT COUNT(p) FROM Patient p WHERE p.epaSyncStatus = 'error'", Long.class))
                .thenReturn(countQuery);

        Response resp = epaResource.getEPAStatistics();
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        String body = (String) resp.getEntity();
        assertTrue(body.contains("\"total\":") || body.contains("total"));
    }
}
