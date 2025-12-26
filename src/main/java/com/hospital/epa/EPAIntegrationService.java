package com.hospital.epa;

import com.hospital.entity.Patient;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.logging.Logger;

/**
 * Service für die Kommunikation mit der elektronischen Patientenakte (EPA)
 * Unterstützt FHIR-Standard für Interoperabilität
 */
@Stateless
public class EPAIntegrationService {
    
    private static final Logger LOGGER = Logger.getLogger(EPAIntegrationService.class.getName());
    
    // EPA-System Basis-URL (konfigurierbar über Umgebungsvariablen)
    private static final String EPA_BASE_URL = System.getenv()
        .getOrDefault("EPA_BASE_URL", "https://epa-system.example.com/api");
    
    private static final String EPA_API_KEY = System.getenv()
        .getOrDefault("EPA_API_KEY", "your-api-key");
    
    @Inject
    private FHIRConverter fhirConverter;
    
    /**
     * Sendet Patientendaten an die EPA
     */
    public EPAResponse sendPatientToEPA(Patient patient) {
        LOGGER.info("Sende Patient " + patient.getId() + " an EPA");
        
        try {
            // Konvertiere Patient zu FHIR-Format
            String fhirPatient = fhirConverter.patientToFHIR(patient);
            
            Client client = ClientBuilder.newClient();
            Response response = client.target(EPA_BASE_URL + "/Patient")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + EPA_API_KEY)
                .header("Content-Type", "application/fhir+json")
                .post(Entity.json(fhirPatient));
            
            if (response.getStatus() == 201 || response.getStatus() == 200) {
                String epaId = response.readEntity(String.class);
                LOGGER.info("Patient erfolgreich an EPA gesendet. EPA-ID: " + epaId);
                return new EPAResponse(true, epaId, "Patient erfolgreich übertragen");
            } else {
                String error = response.readEntity(String.class);
                LOGGER.warning("EPA-Fehler: " + error);
                return new EPAResponse(false, null, "Fehler bei EPA-Übertragung: " + error);
            }
            
        } catch (Exception e) {
            LOGGER.severe("Fehler bei EPA-Übertragung: " + e.getMessage());
            return new EPAResponse(false, null, "Technischer Fehler: " + e.getMessage());
        }
    }
    
    /**
     * Aktualisiert Patientendaten in der EPA
     */
    public EPAResponse updatePatientInEPA(Patient patient, String epaId) {
        LOGGER.info("Aktualisiere Patient " + patient.getId() + " in EPA (EPA-ID: " + epaId + ")");
        
        try {
            String fhirPatient = fhirConverter.patientToFHIR(patient);
            
            Client client = ClientBuilder.newClient();
            Response response = client.target(EPA_BASE_URL + "/Patient/" + epaId)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + EPA_API_KEY)
                .header("Content-Type", "application/fhir+json")
                .put(Entity.json(fhirPatient));
            
            if (response.getStatus() == 200) {
                LOGGER.info("Patient erfolgreich in EPA aktualisiert");
                return new EPAResponse(true, epaId, "Patient erfolgreich aktualisiert");
            } else {
                String error = response.readEntity(String.class);
                return new EPAResponse(false, null, "Fehler bei EPA-Update: " + error);
            }
            
        } catch (Exception e) {
            LOGGER.severe("Fehler bei EPA-Update: " + e.getMessage());
            return new EPAResponse(false, null, "Technischer Fehler: " + e.getMessage());
        }
    }
    
    /**
     * Ruft Patientendaten aus der EPA ab
     */
    public String getPatientFromEPA(String epaId) {
        LOGGER.info("Rufe Patient mit EPA-ID " + epaId + " ab");
        
        try {
            Client client = ClientBuilder.newClient();
            Response response = client.target(EPA_BASE_URL + "/Patient/" + epaId)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + EPA_API_KEY)
                .get();
            
            if (response.getStatus() == 200) {
                return response.readEntity(String.class);
            } else {
                LOGGER.warning("Patient nicht in EPA gefunden");
                return null;
            }
            
        } catch (Exception e) {
            LOGGER.severe("Fehler beim EPA-Abruf: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Synchronisiert alle aktiven Patienten mit der EPA
     */
    public EPASyncResult syncAllPatientsToEPA(java.util.List<Patient> patients) {
        int success = 0;
        int failed = 0;
        
        for (Patient patient : patients) {
            EPAResponse response = sendPatientToEPA(patient);
            if (response.isSuccess()) {
                success++;
            } else {
                failed++;
            }
        }
        
        return new EPASyncResult(success, failed);
    }
    
    /**
     * Prüft die Verbindung zur EPA
     */
    public boolean testEPAConnection() {
        try {
            Client client = ClientBuilder.newClient();
            Response response = client.target(EPA_BASE_URL + "/health")
                .request()
                .header("Authorization", "Bearer " + EPA_API_KEY)
                .get();
            
            return response.getStatus() == 200;
        } catch (Exception e) {
            LOGGER.severe("EPA-Verbindungstest fehlgeschlagen: " + e.getMessage());
            return false;
        }
    }
}

/**
 * Response-Objekt für EPA-Operationen
 */
class EPAResponse {
    private boolean success;
    private String epaId;
    private String message;
    
    public EPAResponse(boolean success, String epaId, String message) {
        this.success = success;
        this.epaId = epaId;
        this.message = message;
    }
    
    public boolean isSuccess() { return success; }
    public String getEpaId() { return epaId; }
    public String getMessage() { return message; }
}

/**
 * Ergebnis-Objekt für Massen-Synchronisation
 */
class EPASyncResult {
    private int successCount;
    private int failedCount;
    
    public EPASyncResult(int successCount, int failedCount) {
        this.successCount = successCount;
        this.failedCount = failedCount;
    }
    
    public int getSuccessCount() { return successCount; }
    public int getFailedCount() { return failedCount; }
}