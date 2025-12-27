package com.hospital.epa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EPAValueObjectsTest {

    @Test
    @DisplayName("EPAResponse getters return expected values")
    void testEPAResponseGetters() {
        EPAResponse r = new EPAResponse(true, "EPA-1", "ok");
        assertTrue(r.isSuccess());
        assertEquals("EPA-1", r.getEpaId());
        assertEquals("ok", r.getMessage());
    }

    @Test
    @DisplayName("EPASyncResult getters return expected counts")
    void testEPASyncResultGetters() {
        EPASyncResult s = new EPASyncResult(5, 2);
        assertEquals(5, s.getSuccessCount());
        assertEquals(2, s.getFailedCount());
    }
}
