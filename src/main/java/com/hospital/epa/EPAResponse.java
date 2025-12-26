package com.hospital.epa;

/**
 * Public response object for EPA operations.
 */
public class EPAResponse {
    private boolean success;
    private String epaId;
    private String message;

    public EPAResponse(boolean success, String epaId, String message) {
        this.success = success;
        this.epaId = epaId;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getEpaId() {
        return epaId;
    }

    public String getMessage() {
        return message;
    }
}
