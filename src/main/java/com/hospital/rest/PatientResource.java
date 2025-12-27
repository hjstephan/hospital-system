package com.hospital.rest;

import java.util.List;

import com.hospital.entity.Patient;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Stateless
@Path("/patients")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PatientResource {

    @PersistenceContext(unitName = "hospitalPU")
    private EntityManager em;

    @GET
    public Response getAllPatients(@QueryParam("status") String status) {
        try {
            List<Patient> patients;
            if ("active".equals(status)) {
                patients = em.createNamedQuery("Patient.findActive", Patient.class)
                        .getResultList();
            } else {
                patients = em.createNamedQuery("Patient.findAll", Patient.class)
                        .getResultList();
            }
            GenericEntity<List<Patient>> entity = new GenericEntity<List<Patient>>(patients) {
            };
            return Response.ok(entity).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}").build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getPatient(@PathParam("id") Long id) {
        try {
            Patient patient = em.find(Patient.class, id);
            if (patient == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Patient nicht gefunden\"}").build();
            }
            return Response.ok(patient).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}").build();
        }
    }

    @GET
    @Path("/search")
    public Response searchPatients(@QueryParam("q") String query) {
        try {
            List<Patient> patients = em.createNamedQuery("Patient.searchByName", Patient.class)
                    .setParameter("search", "%" + query + "%")
                    .getResultList();
            GenericEntity<List<Patient>> entity = new GenericEntity<List<Patient>>(patients) {
            };
            return Response.ok(entity).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}").build();
        }
    }

    @POST
    public Response createPatient(Patient patient) {
        try {
            em.persist(patient);
            em.flush();
            return Response.status(Response.Status.CREATED).entity(patient).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}").build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updatePatient(@PathParam("id") Long id, Patient updatedPatient) {
        try {
            Patient patient = em.find(Patient.class, id);
            if (patient == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Patient nicht gefunden\"}").build();
            }

            patient.setFirstName(updatedPatient.getFirstName());
            patient.setLastName(updatedPatient.getLastName());
            patient.setDateOfBirth(updatedPatient.getDateOfBirth());
            patient.setGender(updatedPatient.getGender());
            patient.setPhone(updatedPatient.getPhone());
            patient.setEmail(updatedPatient.getEmail());
            patient.setAddress(updatedPatient.getAddress());
            patient.setInsuranceNumber(updatedPatient.getInsuranceNumber());
            patient.setBloodType(updatedPatient.getBloodType());
            patient.setAllergies(updatedPatient.getAllergies());
            patient.setEmergencyContactName(updatedPatient.getEmergencyContactName());
            patient.setEmergencyContactPhone(updatedPatient.getEmergencyContactPhone());
            patient.setStatus(updatedPatient.getStatus());
            patient.setDischargeDate(updatedPatient.getDischargeDate());

            em.merge(patient);
            em.flush();
            return Response.ok(patient).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}").build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deletePatient(@PathParam("id") Long id) {
        try {
            Patient patient = em.find(Patient.class, id);
            if (patient == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Patient nicht gefunden\"}").build();
            }
            em.remove(patient);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}").build();
        }
    }
}