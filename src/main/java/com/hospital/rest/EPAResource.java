package com.hospital.rest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

import com.hospital.entity.Patient;
import com.hospital.epa.EPAIntegrationService;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Stateless
@Path("/epa")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EPAResource {

    private static final Logger LOGGER = Logger.getLogger(EPAResource.class.getName());

    @PersistenceContext(unitName = "hospitalPU")
    private EntityManager em;

    @Inject
    private EPAIntegrationService epaService;

    /**
     * Sendet einen einzelnen Patienten an die EPA
     */
    @POST
    @Path("/sync/{patientId}")
    public Response syncPatientToEPA(@PathParam("patientId") Long patientId) {
        try {
            Patient patient = em.find(Patient.class, patientId);
            if (patient == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Patient nicht gefunden\"}").build();
            }

            // Prüfe EPA-Einwilligung
            if (!Boolean.TRUE.equals(patient.getEpaEnabled())) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity("{\"error\": \"Patient hat keine EPA-Einwilligung erteilt\"}").build();
            }

            // Sende an EPA
            var result = epaService.sendPatientToEPA(patient);

            if (result.isSuccess()) {
                // Aktualisiere Patient mit EPA-Informationen
                patient.setEpaId(result.getEpaId());
                patient.setEpaSyncStatus("synced");
                patient.setEpaLastSync(LocalDateTime.now());
                patient.setEpaSyncError(null);
                em.merge(patient);
                em.flush();

                return Response.ok()
                        .entity("{\"success\": true, \"message\": \"" + result.getMessage() +
                                "\", \"epaId\": \"" + result.getEpaId() + "\"}")
                        .build();
            } else {
                // Speichere Fehler
                patient.setEpaSyncStatus("error");
                patient.setEpaSyncError(result.getMessage());
                em.merge(patient);
                em.flush();

                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"success\": false, \"error\": \"" + result.getMessage() + "\"}").build();
            }

        } catch (Exception e) {
            LOGGER.severe("Fehler bei EPA-Sync: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}").build();
        }
    }

    /**
     * Synchronisiert alle Patienten mit der EPA
     */
    @POST
    @Path("/sync-all")
    public Response syncAllPatientsToEPA() {
        try {
            List<Patient> patients = em.createNamedQuery("Patient.findActive", Patient.class)
                    .getResultList();

            // Filtere nur Patienten mit EPA-Einwilligung
            List<Patient> epaEnabledPatients = patients.stream()
                    .filter(p -> Boolean.TRUE.equals(p.getEpaEnabled()))
                    .toList();

            if (epaEnabledPatients.isEmpty()) {
                return Response.ok()
                        .entity("{\"message\": \"Keine Patienten mit EPA-Einwilligung gefunden\"}").build();
            }

            var result = epaService.syncAllPatientsToEPA(epaEnabledPatients);

            return Response.ok()
                    .entity("{\"success\": " + result.getSuccessCount() +
                            ", \"failed\": " + result.getFailedCount() +
                            ", \"total\": " + epaEnabledPatients.size() + "}")
                    .build();

        } catch (Exception e) {
            LOGGER.severe("Fehler bei Massen-EPA-Sync: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}").build();
        }
    }

    /**
     * Aktiviert EPA für einen Patienten (Einwilligungserklärung)
     */
    @PUT
    @Path("/consent/{patientId}")
    public Response setEPAConsent(@PathParam("patientId") Long patientId,
            @QueryParam("enabled") boolean enabled) {
        try {
            Patient patient = em.find(Patient.class, patientId);
            if (patient == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Patient nicht gefunden\"}").build();
            }

            patient.setEpaEnabled(enabled);
            if (enabled) {
                patient.setEpaConsentDate(LocalDateTime.now());
                patient.setEpaSyncStatus("pending");
            } else {
                patient.setEpaSyncStatus("disabled");
            }

            em.merge(patient);
            em.flush();

            String message = enabled ? "EPA-Einwilligung erteilt" : "EPA-Einwilligung widerrufen";
            return Response.ok()
                    .entity("{\"success\": true, \"message\": \"" + message + "\"}").build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}").build();
        }
    }

    /**
     * Ruft EPA-Status für einen Patienten ab
     */
    @GET
    @Path("/status/{patientId}")
    public Response getEPAStatus(@PathParam("patientId") Long patientId) {
        try {
            Patient patient = em.find(Patient.class, patientId);
            if (patient == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Patient nicht gefunden\"}").build();
            }

            String response = String.format(
                    "{\"epaEnabled\": %b, \"epaId\": \"%s\", \"syncStatus\": \"%s\", " +
                            "\"lastSync\": \"%s\", \"syncError\": \"%s\"}",
                    patient.getEpaEnabled(),
                    patient.getEpaId() != null ? patient.getEpaId() : "",
                    patient.getEpaSyncStatus(),
                    patient.getEpaLastSync() != null ? patient.getEpaLastSync().toString() : "",
                    patient.getEpaSyncError() != null ? patient.getEpaSyncError() : "");

            return Response.ok(response).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}").build();
        }
    }

    /**
     * Ruft Patientendaten aus der EPA ab
     */
    @GET
    @Path("/fetch/{epaId}")
    public Response fetchFromEPA(@PathParam("epaId") String epaId) {
        try {
            String fhirData = epaService.getPatientFromEPA(epaId);

            if (fhirData != null) {
                return Response.ok(fhirData).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Patient nicht in EPA gefunden\"}").build();
            }

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}").build();
        }
    }

    /**
     * Testet die EPA-Verbindung
     */
    @GET
    @Path("/test-connection")
    public Response testConnection() {
        try {
            boolean connected = epaService.testEPAConnection();

            if (connected) {
                return Response.ok()
                        .entity("{\"connected\": true, \"message\": \"EPA-Verbindung erfolgreich\"}").build();
            } else {
                return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity("{\"connected\": false, \"message\": \"EPA nicht erreichbar\"}").build();
            }

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}").build();
        }
    }

    /**
     * Liefert Statistiken zur EPA-Synchronisation
     */
    @GET
    @Path("/statistics")
    public Response getEPAStatistics() {
        try {
            Long totalPatients = em.createQuery("SELECT COUNT(p) FROM Patient p", Long.class)
                    .getSingleResult();

            Long epaEnabledCount = em.createQuery(
                    "SELECT COUNT(p) FROM Patient p WHERE p.epaEnabled = true", Long.class)
                    .getSingleResult();

            Long syncedCount = em.createQuery(
                    "SELECT COUNT(p) FROM Patient p WHERE p.epaSyncStatus = 'synced'", Long.class)
                    .getSingleResult();

            Long errorCount = em.createQuery(
                    "SELECT COUNT(p) FROM Patient p WHERE p.epaSyncStatus = 'error'", Long.class)
                    .getSingleResult();

            String response = String.format(
                    "{\"total\": %d, \"epaEnabled\": %d, \"synced\": %d, \"errors\": %d}",
                    totalPatients, epaEnabledCount, syncedCount, errorCount);

            return Response.ok(response).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}").build();
        }
    }
}