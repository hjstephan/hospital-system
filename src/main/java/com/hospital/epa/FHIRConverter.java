package com.hospital.epa;

import com.hospital.entity.Patient;

import jakarta.ejb.Stateless;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

/**
 * Konverter für FHIR-Standard (Fast Healthcare Interoperability Resources)
 * Konvertiert interne Patientendaten in FHIR R4 Format
 */
@Stateless
public class FHIRConverter {

    /**
     * Konvertiert einen Patient in FHIR R4 JSON-Format
     */
    public String patientToFHIR(Patient patient) {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        // FHIR Resource Type
        builder.add("resourceType", "Patient");

        // Identifier (Versicherungsnummer)
        JsonArrayBuilder identifierArray = Json.createArrayBuilder();
        identifierArray.add(Json.createObjectBuilder()
                .add("use", "official")
                .add("system", "urn:oid:1.2.276.0.76.4.8")
                .add("value", patient.getInsuranceNumber()));
        builder.add("identifier", identifierArray);

        // Name
        JsonArrayBuilder nameArray = Json.createArrayBuilder();
        nameArray.add(Json.createObjectBuilder()
                .add("use", "official")
                .add("family", patient.getLastName())
                .add("given", Json.createArrayBuilder().add(patient.getFirstName())));
        builder.add("name", nameArray);

        // Geschlecht (FHIR: male, female, other, unknown)
        String fhirGender = convertGenderToFHIR(patient.getGender());
        builder.add("gender", fhirGender);

        // Geburtsdatum
        builder.add("birthDate", patient.getDateOfBirth().toString());

        // Kontaktdaten
        JsonArrayBuilder telecomArray = Json.createArrayBuilder();
        if (patient.getPhone() != null && !patient.getPhone().isEmpty()) {
            telecomArray.add(Json.createObjectBuilder()
                    .add("system", "phone")
                    .add("value", patient.getPhone())
                    .add("use", "home"));
        }
        if (patient.getEmail() != null && !patient.getEmail().isEmpty()) {
            telecomArray.add(Json.createObjectBuilder()
                    .add("system", "email")
                    .add("value", patient.getEmail()));
        }
        builder.add("telecom", telecomArray);

        // Adresse
        if (patient.getAddress() != null && !patient.getAddress().isEmpty()) {
            JsonArrayBuilder addressArray = Json.createArrayBuilder();
            addressArray.add(Json.createObjectBuilder()
                    .add("use", "home")
                    .add("type", "physical")
                    .add("text", patient.getAddress()));
            builder.add("address", addressArray);
        }

        // Status (active/inactive)
        boolean isActive = "active".equals(patient.getStatus());
        builder.add("active", isActive);

        // Extensions für zusätzliche Daten
        JsonArrayBuilder extensionArray = Json.createArrayBuilder();

        // Blutgruppe als Extension
        if (patient.getBloodType() != null && !patient.getBloodType().isEmpty()) {
            extensionArray.add(Json.createObjectBuilder()
                    .add("url", "http://hospital.example.com/fhir/StructureDefinition/blood-type")
                    .add("valueString", patient.getBloodType()));
        }

        // Allergien als Extension
        if (patient.getAllergies() != null && !patient.getAllergies().isEmpty()) {
            extensionArray.add(Json.createObjectBuilder()
                    .add("url", "http://hospital.example.com/fhir/StructureDefinition/allergies")
                    .add("valueString", patient.getAllergies()));
        }

        builder.add("extension", extensionArray);

        // Notfallkontakt
        if (patient.getEmergencyContactName() != null && !patient.getEmergencyContactName().isEmpty()) {
            JsonArrayBuilder contactArray = Json.createArrayBuilder();
            JsonObjectBuilder contactBuilder = Json.createObjectBuilder()
                    .add("relationship", Json.createArrayBuilder()
                            .add(Json.createObjectBuilder()
                                    .add("coding", Json.createArrayBuilder()
                                            .add(Json.createObjectBuilder()
                                                    .add("system", "http://terminology.hl7.org/CodeSystem/v2-0131")
                                                    .add("code", "C")
                                                    .add("display", "Emergency Contact")))));

            contactBuilder.add("name", Json.createObjectBuilder()
                    .add("text", patient.getEmergencyContactName()));

            if (patient.getEmergencyContactPhone() != null && !patient.getEmergencyContactPhone().isEmpty()) {
                contactBuilder.add("telecom", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("system", "phone")
                                .add("value", patient.getEmergencyContactPhone())));
            }

            contactArray.add(contactBuilder);
            builder.add("contact", contactArray);
        }

        JsonObject fhirPatient = builder.build();
        return fhirPatient.toString();
    }

    /**
     * Konvertiert Geschlecht in FHIR-Format
     */
    private String convertGenderToFHIR(String gender) {
        if (gender == null)
            return "unknown";

        switch (gender.toLowerCase()) {
            case "männlich":
            case "male":
                return "male";
            case "weiblich":
            case "female":
                return "female";
            case "divers":
            case "other":
                return "other";
            default:
                return "unknown";
        }
    }

    /**
     * Konvertiert FHIR-Patient zurück in internes Format
     */
    public Patient fhirToPatient(JsonObject fhirPatient) {
        Patient patient = new Patient();

        // Name extrahieren
        if (fhirPatient.containsKey("name")) {
            JsonObject name = fhirPatient.getJsonArray("name").getJsonObject(0);
            patient.setLastName(name.getString("family"));
            patient.setFirstName(name.getJsonArray("given").getString(0));
        }

        // Geburtsdatum
        if (fhirPatient.containsKey("birthDate")) {
            patient.setDateOfBirth(java.time.LocalDate.parse(fhirPatient.getString("birthDate")));
        }

        // Geschlecht
        if (fhirPatient.containsKey("gender")) {
            String fhirGender = fhirPatient.getString("gender");
            patient.setGender(convertFHIRToGender(fhirGender));
        }

        // Identifier (Versicherungsnummer)
        if (fhirPatient.containsKey("identifier")) {
            JsonObject identifier = fhirPatient.getJsonArray("identifier").getJsonObject(0);
            patient.setInsuranceNumber(identifier.getString("value"));
        }

        // Status
        if (fhirPatient.containsKey("active")) {
            boolean active = fhirPatient.getBoolean("active");
            patient.setStatus(active ? "active" : "discharged");
        }

        return patient;
    }

    /**
     * Konvertiert FHIR-Geschlecht zurück
     */
    private String convertFHIRToGender(String fhirGender) {
        switch (fhirGender) {
            case "male":
                return "Männlich";
            case "female":
                return "Weiblich";
            case "other":
                return "Divers";
            default:
                return "Unbekannt";
        }
    }

    /**
     * Erstellt eine FHIR Bundle-Resource für mehrere Patienten
     */
    public String createPatientBundle(java.util.List<Patient> patients) {
        JsonObjectBuilder bundle = Json.createObjectBuilder();
        bundle.add("resourceType", "Bundle");
        bundle.add("type", "collection");
        bundle.add("total", patients.size());

        JsonArrayBuilder entries = Json.createArrayBuilder();
        for (Patient patient : patients) {
            entries.add(Json.createObjectBuilder()
                    .add("resource", Json.createReader(
                            new java.io.StringReader(patientToFHIR(patient))).readObject()));
        }

        bundle.add("entry", entries);
        return bundle.build().toString();
    }
}