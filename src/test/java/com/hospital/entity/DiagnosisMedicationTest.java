package com.hospital.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DiagnosisMedicationTest {

    @Test
    @DisplayName("Diagnosis getters and setters should work")
    void testDiagnosisFields() {
        Diagnosis d = new Diagnosis();
        assertNull(d.getId());

        d.setId(10L);
        d.setDescription("Acute bronchitis");
        LocalDateTime now = LocalDateTime.now();
        d.setDiagnosisDate(now);

        Patient p = new Patient();
        p.setId(1L);
        d.setPatient(p);

        assertEquals(10L, d.getId());
        assertEquals("Acute bronchitis", d.getDescription());
        assertEquals(now, d.getDiagnosisDate());
        assertEquals(p, d.getPatient());
    }

    @Test
    @DisplayName("Medication getters and setters should work")
    void testMedicationFields() {
        Medication m = new Medication();
        assertNull(m.getId());

        m.setId(5L);
        m.setName("Ibuprofen");
        m.setDosage("200mg");
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 10);
        m.setStartDate(start);
        m.setEndDate(end);

        Patient p = new Patient();
        p.setId(2L);
        m.setPatient(p);

        assertEquals(5L, m.getId());
        assertEquals("Ibuprofen", m.getName());
        assertEquals("200mg", m.getDosage());
        assertEquals(start, m.getStartDate());
        assertEquals(end, m.getEndDate());
        assertEquals(p, m.getPatient());
    }
}
