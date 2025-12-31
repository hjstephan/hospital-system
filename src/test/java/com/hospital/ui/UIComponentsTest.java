package com.hospital.ui;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.hospital.pages.PatientManagementPage;

public class UIComponentsTest extends BaseUITest {

    @Test
    public void testHeaderPresent() {
        WebElement header = driver.findElement(By.tagName("header"));
        assertTrue(header.isDisplayed());
        assertTrue(header.getText().contains("Krankenhaus"));
    }

    @Test
    public void testFooterPresent() {
        WebElement footer = driver.findElement(By.tagName("footer"));
        assertTrue(footer.isDisplayed());
        assertTrue(footer.getText().contains("2025"));
    }

    @Test
    public void testEPAStatusBanner() {
        PatientManagementPage page = new PatientManagementPage(driver);

        WebElement banner = driver.findElement(By.id("epaStatusBanner"));
        assertTrue(banner.isDisplayed());

        String statusText = page.getEPAStatusText();
        assertNotNull(statusText);
    }

    @Test
    public void testAllControlButtons() {
        assertTrue(driver.findElement(
                By.xpath("//button[contains(text(), 'Neuer Patient')]")).isDisplayed());
        assertTrue(driver.findElement(
                By.xpath("//button[contains(text(), 'Aktualisieren')]")).isDisplayed());
        assertTrue(driver.findElement(
                By.xpath("//button[contains(text(), 'EPA-Statistiken')]")).isDisplayed());
        assertTrue(driver.findElement(
                By.xpath("//button[contains(text(), 'Alle zu EPA')]")).isDisplayed());
    }

    @Test
    public void testSearchInput() {
        WebElement searchInput = driver.findElement(By.id("searchInput"));
        assertTrue(searchInput.isDisplayed());
        assertTrue(searchInput.isEnabled());

        String placeholder = searchInput.getAttribute("placeholder");
        assertTrue(placeholder.contains("suchen"));
    }

    @Test
    public void testModalStructure() {
        PatientManagementPage page = new PatientManagementPage(driver);

        page.clickNewPatient();

        // Check modal header
        WebElement modalHeader = driver.findElement(
                By.className("modal-header"));
        assertTrue(modalHeader.isDisplayed());

        // Check modal body
        WebElement modalBody = driver.findElement(
                By.className("modal-body"));
        assertTrue(modalBody.isDisplayed());

        // Check all form fields exist
        assertTrue(driver.findElement(By.id("firstName")).isDisplayed());
        assertTrue(driver.findElement(By.id("lastName")).isDisplayed());
        assertTrue(driver.findElement(By.id("dateOfBirth")).isDisplayed());
        assertTrue(driver.findElement(By.id("gender")).isDisplayed());
        assertTrue(driver.findElement(By.id("insuranceNumber")).isDisplayed());
        assertTrue(driver.findElement(By.id("epaEnabled")).isDisplayed());
    }

    @Test
    public void testResponsiveness() {
        // Test desktop size
        driver.manage().window().setSize(new org.openqa.selenium.Dimension(1920, 1080));
        waitForMillis(500);
        assertTrue(driver.findElement(By.tagName("table")).isDisplayed());

        // Test tablet size
        driver.manage().window().setSize(new org.openqa.selenium.Dimension(768, 1024));
        waitForMillis(500);
        assertTrue(driver.findElement(By.tagName("table")).isDisplayed());

        // Test mobile size
        driver.manage().window().setSize(new org.openqa.selenium.Dimension(375, 667));
        waitForMillis(500);
        assertTrue(driver.findElement(By.tagName("table")).isDisplayed());
    }
}