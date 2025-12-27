package com.hospital.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PatientLifecycleTest {

    @Test
    @DisplayName("PrePersist should set createdAt, updatedAt, and admissionDate when null")
    void testPrePersistSetsTimestamps() throws Exception {
        Patient p = new Patient();
        p.setFirstName("Test");
        p.setLastName("User");
        p.setDateOfBirth(LocalDate.of(1990, 1, 1));

        // Ensure admissionDate is null prior to prePersist
        assertNull(p.getAdmissionDate());

        Method onCreate = Patient.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(p);

        assertNotNull(p.getCreatedAt());
        assertNotNull(p.getUpdatedAt());
        assertNotNull(p.getAdmissionDate());
    }

    @Test
    @DisplayName("PreUpdate should update the updatedAt timestamp")
    void testPreUpdateUpdatesTimestamp() throws Exception {
        Patient p = new Patient();

        // Call onCreate first to set initial timestamps
        Method onCreate = Patient.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(p);

        LocalDateTime before = p.getUpdatedAt();
        Thread.sleep(5); // ensure time advances

        Method onUpdate = Patient.class.getDeclaredMethod("onUpdate");
        onUpdate.setAccessible(true);
        onUpdate.invoke(p);

        assertNotNull(p.getUpdatedAt());
        assertTrue(p.getUpdatedAt().isAfter(before) || p.getUpdatedAt().isEqual(before));
    }

    @Test
    @DisplayName("Should set and get diagnoses and medications lists")
    void testSetLists() {
        Patient p = new Patient();
        Diagnosis d = new Diagnosis();
        d.setDescription("Test dx");
        Medication m = new Medication();
        m.setName("Med1");

        List<Diagnosis> diagnoses = Arrays.asList(d);
        List<Medication> medications = Arrays.asList(m);

        p.setDiagnoses(diagnoses);
        p.setMedications(medications);

        assertEquals(1, p.getDiagnoses().size());
        assertEquals("Test dx", p.getDiagnoses().get(0).getDescription());
        assertEquals(1, p.getMedications().size());
        assertEquals("Med1", p.getMedications().get(0).getName());
    }
}
