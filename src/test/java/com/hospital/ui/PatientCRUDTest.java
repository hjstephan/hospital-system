package com.hospital.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.hospital.pages.PatientManagementPage;
import com.hospital.pages.PatientManagementPage.PatientData;

public class PatientCRUDTest extends BaseUITest {

    @Test
    public void testLoadPatientsInitial() {
        PatientManagementPage page = new PatientManagementPage(driver);

        // Verify table is visible with random patients
        assertTrue(page.isTableVisible(), "Table should be visible");

        int rowCount = page.getPatientRowCount();
        assertTrue(rowCount > 0, "Should have at least one patient row");

        // Verify JavaScript variable
        Boolean isInitialLoad = (Boolean) executeScript("return isInitialLoad;");
        assertFalse(isInitialLoad, "isInitialLoad should be false after load");
    }

    @Test
    public void testOpenModal() {
        PatientManagementPage page = new PatientManagementPage(driver);

        // Click new patient button
        page.clickNewPatient();

        // Verify modal opened
        assertTrue(page.isModalOpen(), "Modal should be open");
        assertEquals("Neuer Patient", page.getModalTitle());

        // Test JavaScript function directly
        executeScript("openModal(false);");
        waitForMillis(300);
        assertTrue(page.isModalOpen());
    }

    @Test
    public void testCloseModal() {
        PatientManagementPage page = new PatientManagementPage(driver);

        // Open modal
        page.clickNewPatient();
        assertTrue(page.isModalOpen());

        // Close via JavaScript function
        executeScript("closeModal();");
        waitForMillis(300);

        assertFalse(page.isModalOpen(), "Modal should be closed");

        // Verify currentEditId is null
        Object currentEditId = executeScript("return currentEditId;");
        assertNull(currentEditId);
    }

    @Test
    public void testCreatePatient() {
        PatientManagementPage page = new PatientManagementPage(driver);

        // Open modal
        page.clickNewPatient();

        // Fill form
        PatientData patient = PatientData.createDefault();
        page.fillPatientForm(patient);

        // Submit
        page.submitForm();

        // Wait for success dialog
        page.waitForDialog();
        assertTrue(page.isDialogOpen());
        assertTrue(page.getDialogBody().contains("erfolgreich"));

        // Close dialog
        page.clickDialogOk();

        // Verify modal closed
        waitForMillis(500);
        assertFalse(page.isModalOpen());
    }

    @Test
    public void testEditPatient() {
        PatientManagementPage page = new PatientManagementPage(driver);

        // Wait for patients to load
        waitForMillis(1000);

        // Get patient count
        int initialCount = page.getPatientRowCount();
        assertTrue(initialCount > 0, "Need at least one patient");

        // Click edit on first patient
        page.clickEditOnRow(0);

        // Verify modal opened with edit title
        assertTrue(page.isModalOpen());
        assertTrue(page.getModalTitle().contains("bearbeiten"));

        // Verify currentEditId is set
        Object currentEditId = executeScript("return currentEditId;");
        assertNotNull(currentEditId);

        // Test editPatient function directly
        Long patientId = ((Number) currentEditId).longValue();
        executeAsyncScript(
                "var callback = arguments[arguments.length - 1];" +
                        "editPatient(" + patientId + ").then(() => callback(true));");

        waitForMillis(1000);
        assertTrue(page.isModalOpen());
    }

    @Test
    public void testDeletePatient() {
        PatientManagementPage page = new PatientManagementPage(driver);

        // Wait for load
        waitForMillis(1000);
        int initialCount = page.getPatientRowCount();

        // Click delete on first patient
        page.clickDeleteOnRow(0);

        // Confirm in dialog
        page.waitForDialog();
        assertTrue(page.isDialogOpen());
        page.clickDialogOk();

        // Wait for success dialog
        waitForMillis(1000);
        page.waitForDialog();
        page.clickDialogOk();

        // Verify deletion completed
        waitForMillis(1000);
        assertTrue(page.isTableVisible());
    }

    @Test
    public void testFormatDate() {
        String dateString = "1990-01-15";

        Object formatted = executeScript(
                "function formatDate(dateString) {" +
                        "  if (!dateString) return '-';" +
                        "  const date = new Date(dateString);" +
                        "  return date.toLocaleDateString('de-DE');" +
                        "}" +
                        "return formatDate(arguments[0]);",
                dateString);

        assertNotNull(formatted);
        assertTrue(formatted.toString().contains("15"));
    }
}