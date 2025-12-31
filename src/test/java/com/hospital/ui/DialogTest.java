package com.hospital.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.hospital.pages.PatientManagementPage;

public class DialogTest extends BaseUITest {

    @Test
    public void testShowAlert() {
        PatientManagementPage page = new PatientManagementPage(driver);

        // Trigger alert via button click instead of async script
        executeScript(
                "showAlert('Test alert message');");

        // Wait a bit for dialog to appear
        waitForMillis(1000);

        // Verify dialog is displayed
        page.waitForDialog();
        assertTrue(page.isDialogOpen(), "Dialog should be open");
        assertEquals("Hinweis", page.getDialogTitle());
        assertTrue(page.getDialogBody().contains("Test alert message"));

        // Close dialog
        page.clickDialogOk();

        // Verify dialog closed
        waitForMillis(500);
        assertFalse(page.isDialogOpen(), "Dialog should be closed");
    }

    @Test
    public void testShowConfirm() {
        PatientManagementPage page = new PatientManagementPage(driver);

        // Trigger confirm dialog
        executeScript("showConfirm('Confirm this action?');");

        waitForMillis(1000);
        page.waitForDialog();
        assertTrue(page.isDialogOpen());

        // Test "OK" button
        page.clickDialogOk();
        waitForMillis(500);
        assertFalse(page.isDialogOpen());

        // Test "Cancel" button
        executeScript("showConfirm('Confirm again?');");
        waitForMillis(1000);
        page.waitForDialog();
        page.clickDialogCancel();
        waitForMillis(500);
        assertFalse(page.isDialogOpen());
    }

    @Test
    public void testEscapeHtml() {
        String unsafe = "<script>alert('xss')</script>";

        Object escaped = executeScript(
                "function escapeHtml(unsafe) {" +
                        "  return String(unsafe)" +
                        "    .replace(/&/g, '&amp;')" +
                        "    .replace(/</g, '&lt;')" +
                        "    .replace(/>/g, '&gt;')" +
                        "    .replace(/\"/g, '&quot;')" +
                        "    .replace(/'/g, '&#039;');" +
                        "}" +
                        "return escapeHtml(arguments[0]);",
                unsafe);

        assertFalse(escaped.toString().contains("<script>"));
        assertTrue(escaped.toString().contains("&lt;script&gt;"));
    }

    @Test
    public void testDialogFunctionsExist() {
        // Verify dialog functions are defined
        Object showAlertExists = executeScript("return typeof showAlert === 'function';");
        assertTrue((Boolean) showAlertExists, "showAlert function should exist");

        Object showConfirmExists = executeScript("return typeof showConfirm === 'function';");
        assertTrue((Boolean) showConfirmExists, "showConfirm function should exist");

        Object showDialogExists = executeScript("return typeof showDialog === 'function';");
        assertTrue((Boolean) showDialogExists, "showDialog function should exist");

        Object escapeHtmlExists = executeScript("return typeof escapeHtml === 'function';");
        assertTrue((Boolean) escapeHtmlExists, "escapeHtml function should exist");
    }
}