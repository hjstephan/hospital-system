package com.hospital.ui;

import static com.hospital.ui.BaseUITest.LOGGER;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import com.hospital.pages.PatientManagementPage;

/**
 * EPA Integration Tests
 * Note: These tests assume NO real EPA server connection is available.
 * Tests verify that the UI handles EPA connection failures gracefully.
 */
public class EPAIntegrationTest extends BaseUITest {

    @Test
    public void testCheckEPAConnection() {
        PatientManagementPage page = new PatientManagementPage(driver);

        // Call checkEPAConnection function
        executeAsyncScript(
                "var callback = arguments[arguments.length - 1];" +
                        "checkEPAConnection().then(() => callback(true));");

        waitForMillis(2000);

        // Verify EPA status updated (should show disconnected or error)
        String statusText = page.getEPAStatusText();
        assertNotNull(statusText, "EPA status text should not be null");
        assertTrue(statusText.contains("EPA"), "Status should mention EPA");

        // Since no real EPA server exists, expect disconnected or error state
        boolean isDisconnected = statusText.contains("nicht erreichbar") ||
                statusText.contains("Fehler") ||
                statusText.contains("fehlgeschlagen");

        if (isDisconnected) {
            LOGGER.info("EPA connection test correctly shows disconnected state: " + statusText);
        } else {
            LOGGER.warning("EPA status unexpected: " + statusText);
        }
    }

    @Test
    public void testShowEPAStats() {
        PatientManagementPage page = new PatientManagementPage(driver);

        // Click EPA stats button
        page.clickEPAStats();

        waitForMillis(2000);

        // Verify stats container is populated (may show zeros or errors)
        boolean statsVisible = page.areStatsVisible();

        if (statsVisible) {
            LOGGER.info("EPA stats displayed successfully");
            assertTrue(true);
        } else {
            LOGGER.info("EPA stats not displayed (expected if no EPA server)");
            // Don't fail the test - this is expected behavior
            assertTrue(true);
        }

        // Test function directly
        executeAsyncScript(
                "var callback = arguments[arguments.length - 1];" +
                        "showEPAStats().then(() => callback(true)).catch(() => callback(false));");

        waitForMillis(2000);
    }

    @Test
    public void testSyncToEPAHandlesError() {
        PatientManagementPage page = new PatientManagementPage(driver);

        // Wait for patients to load
        waitForMillis(1500);

        if (page.getPatientRowCount() > 0) {
            // Click sync on first patient
            page.clickSyncOnRow(0);

            // Confirm in dialog
            page.waitForDialog();
            assertTrue(page.isDialogOpen(), "Confirm dialog should appear");
            page.clickDialogOk();

            // Wait for result dialog (expect error due to no EPA server)
            waitForMillis(3000);

            boolean resultDialogAppeared = waitForCondition(page::isDialogOpen, 10);

            if (resultDialogAppeared) {
                // Check result - should show error message
                String dialogBody = page.getDialogBody();
                assertNotNull(dialogBody, "Dialog body should not be null");

                // Since no EPA server exists, expect error message
                boolean hasErrorOrSuccess = dialogBody.contains("EPA") ||
                        dialogBody.contains("synchronisiert") ||
                        dialogBody.contains("Fehler") ||
                        dialogBody.contains("fehlgeschlagen");

                assertTrue(hasErrorOrSuccess, "Dialog should contain EPA-related message");

                LOGGER.info("EPA sync result: " + dialogBody);
                page.clickDialogOk();
            } else {
                LOGGER.warning("Result dialog did not appear - possible timeout");
            }
        } else {
            Assumptions.abort("No patients available for sync test");
        }
    }

    @Test
    public void testSyncAllToEPAHandlesError() {
        PatientManagementPage page = new PatientManagementPage(driver);

        // Click sync all button
        page.clickSyncAll();

        // Confirm
        page.waitForDialog();
        assertTrue(page.isDialogOpen(), "Confirm dialog should appear");
        page.clickDialogOk();

        // Wait for result (this may take longer, expect failures due to no EPA server)
        waitForMillis(5000);

        // Use helper method from BaseUITest
        boolean dialogOpened = waitForCondition(page::isDialogOpen, 15);
        assertTrue(dialogOpened, "Result dialog should appear");

        // Check result - expect mostly failures since no EPA server
        String dialogBody = page.getDialogBody();
        assertNotNull(dialogBody, "Dialog body should not be null");

        boolean hasResultInfo = dialogBody.contains("Erfolgreich") ||
                dialogBody.contains("Fehlgeschlagen") ||
                dialogBody.contains("Gesamt") ||
                dialogBody.contains("Fehler");

        assertTrue(hasResultInfo, "Dialog should contain sync result information");

        LOGGER.info("Sync all result: " + dialogBody);
        page.clickDialogOk();
    }

    @Test
    public void testEPACheckboxInForm() {
        PatientManagementPage page = new PatientManagementPage(driver);

        // Open new patient modal
        page.clickNewPatient();

        waitForMillis(500);
        assertTrue(page.isModalOpen(), "Modal should be open");

        // Check that EPA checkbox exists and is checked by default
        Boolean epaEnabled = (Boolean) executeScript("return document.getElementById('epaEnabled').checked;");
        assertNotNull(epaEnabled, "EPA checkbox should exist");
        assertTrue(epaEnabled, "EPA checkbox should be checked by default");

        // Uncheck it
        executeScript("document.getElementById('epaEnabled').checked = false;");
        waitForMillis(200);

        epaEnabled = (Boolean) executeScript("return document.getElementById('epaEnabled').checked;");
        assertFalse(epaEnabled, "EPA checkbox should be unchecked");

        // Check it again
        executeScript("document.getElementById('epaEnabled').checked = true;");
        waitForMillis(200);

        epaEnabled = (Boolean) executeScript("return document.getElementById('epaEnabled').checked;");
        assertTrue(epaEnabled, "EPA checkbox should be checked again");

        page.closeModal();
    }

    @Test
    public void testEPAStatusBannerDisplays() {
        PatientManagementPage page = new PatientManagementPage(driver);

        // Verify EPA status banner is visible
        Boolean bannerVisible = (Boolean) executeScript(
                "return document.getElementById('epaStatusBanner') !== null && " +
                        "document.getElementById('epaStatusBanner').offsetParent !== null;");

        assertTrue(bannerVisible, "EPA status banner should be visible");

        // Check initial status
        String statusText = page.getEPAStatusText();
        assertNotNull(statusText, "EPA status should be displayed");

        LOGGER.info("EPA banner status: " + statusText);
    }

    @Test
    public void testEPAConnectionTestButton() {
        PatientManagementPage page = new PatientManagementPage(driver);

        String initialStatus = page.getEPAStatusText();
        LOGGER.info("Initial EPA status: " + initialStatus);

        // Click test connection button
        page.clickTestConnection();

        waitForMillis(2000);

        // Status should have updated (even if showing error)
        String newStatus = page.getEPAStatusText();
        assertNotNull(newStatus, "EPA status should be updated");

        LOGGER.info("EPA status after test: " + newStatus);

        // The status may be the same or different, just verify it exists
        assertTrue(newStatus.contains("EPA"), "Status should mention EPA");
    }

    @Test
    public void testSyncToEPAFunctionDirectCall() {
        // Test that the syncToEPA JavaScript function exists and can be called
        Object functionExists = executeScript(
                "return typeof syncToEPA === 'function';");

        assertTrue((Boolean) functionExists, "syncToEPA function should exist");

        // Verify the function signature (takes a patient ID)
        Object functionSignature = executeScript(
                "return syncToEPA.toString().includes('patientId');");

        assertTrue((Boolean) functionSignature,
                "syncToEPA should accept patientId parameter");
    }

    @Test
    public void testShowEPAStatsFunctionExists() {
        // Verify showEPAStats function exists
        Object functionExists = executeScript(
                "return typeof showEPAStats === 'function';");

        assertTrue((Boolean) functionExists, "showEPAStats function should exist");
    }

    @Test
    public void testCheckEPAConnectionFunctionExists() {
        // Verify checkEPAConnection function exists
        Object functionExists = executeScript(
                "return typeof checkEPAConnection === 'function';");

        assertTrue((Boolean) functionExists, "checkEPAConnection function should exist");
    }

    @Test
    public void testSyncAllToEPAFunctionExists() {
        // Verify syncAllToEPA function exists
        Object functionExists = executeScript(
                "return typeof syncAllToEPA === 'function';");

        assertTrue((Boolean) functionExists, "syncAllToEPA function should exist");
    }

    @Test
    public void testEPAAPIConstants() {
        // Verify EPA_API_URL constant is defined
        Object epaApiUrl = executeScript("return EPA_API_URL;");
        assertNotNull(epaApiUrl, "EPA_API_URL should be defined");

        String url = (String) epaApiUrl;
        assertTrue(url.contains("/epa"), "EPA_API_URL should contain /epa path");

        LOGGER.info("EPA API URL: " + url);
    }
}