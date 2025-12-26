package com.hospital.epa;

/**
 * Public result object for bulk EPA synchronization.
 */
public class EPASyncResult {
    private int successCount;
    private int failedCount;

    public EPASyncResult(int successCount, int failedCount) {
        this.successCount = successCount;
        this.failedCount = failedCount;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getFailedCount() {
        return failedCount;
    }
}
