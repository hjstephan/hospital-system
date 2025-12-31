package com.hospital.pages;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class PatientManagementPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final JavascriptExecutor js;

    // Locators
    private final By searchInput = By.id("searchInput");
    private final By newPatientButton = By.xpath("//button[contains(text(), 'Neuer Patient')]");
    private final By refreshButton = By.xpath("//button[contains(text(), 'Aktualisieren')]");
    private final By epaStatsButton = By.xpath("//button[contains(text(), 'EPA-Statistiken')]");
    private final By syncAllButton = By.xpath("//button[contains(text(), 'Alle zu EPA')]");
    private final By testConnectionButton = By.xpath("//button[contains(text(), 'Verbindung testen')]");

    private final By patientModal = By.id("patientModal");
    private final By modalTitle = By.id("modalTitle");
    private final By closeModalButton = By.className("close-btn");

    private final By firstNameInput = By.id("firstName");
    private final By lastNameInput = By.id("lastName");
    private final By dateOfBirthInput = By.id("dateOfBirth");
    private final By genderSelect = By.id("gender");
    private final By phoneInput = By.id("phone");
    private final By emailInput = By.id("email");
    private final By addressInput = By.id("address");
    private final By insuranceNumberInput = By.id("insuranceNumber");
    private final By bloodTypeSelect = By.id("bloodType");
    private final By allergiesInput = By.id("allergies");
    private final By emergencyContactNameInput = By.id("emergencyContactName");
    private final By emergencyContactPhoneInput = By.id("emergencyContactPhone");
    private final By statusSelect = By.id("status");
    private final By epaEnabledCheckbox = By.id("epaEnabled");
    private final By submitButton = By.xpath("//button[@type='submit' and contains(text(), 'Speichern')]");
    private final By cancelButton = By.xpath("//button[contains(text(), 'Abbrechen')]");

    private final By dialogModal = By.id("dialogModal");
    private final By dialogTitle = By.id("dialogTitle");
    private final By dialogBody = By.id("dialogBody");
    private final By dialogOkButton = By.id("dialogOkBtn");
    private final By dialogCancelButton = By.id("dialogCancelBtn");

    private final By patientsTable = By.cssSelector("#patientsTable table");
    private final By tableRows = By.cssSelector("#patientsTable tbody tr");
    private final By statsContainer = By.id("statsContainer");
    private final By epaStatusBanner = By.id("epaStatusBanner");
    private final By epaStatusText = By.id("epaStatusText");

    public PatientManagementPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        this.js = (JavascriptExecutor) driver;
    }

    // Navigation Methods

    public void clickNewPatient() {
        wait.until(ExpectedConditions.elementToBeClickable(newPatientButton)).click();
    }

    public void clickRefresh() {
        driver.findElement(refreshButton).click();
    }

    public void clickEPAStats() {
        driver.findElement(epaStatsButton).click();
    }

    public void clickSyncAll() {
        driver.findElement(syncAllButton).click();
    }

    public void clickTestConnection() {
        driver.findElement(testConnectionButton).click();
    }

    // Search Methods

    public void enterSearchQuery(String query) {
        WebElement search = driver.findElement(searchInput);
        search.clear();
        search.sendKeys(query);
    }

    public void clearSearch() {
        WebElement search = driver.findElement(searchInput);
        search.clear();
    }

    public String getSearchValue() {
        return driver.findElement(searchInput).getAttribute("value");
    }

    // Modal Methods

    public boolean isModalOpen() {
        String className = driver.findElement(patientModal).getAttribute("class");
        return className != null && className.contains("active");
    }

    public String getModalTitle() {
        return driver.findElement(modalTitle).getText();
    }

    public void closeModal() {
        driver.findElement(closeModalButton).click();
    }

    public void clickCancel() {
        driver.findElement(cancelButton).click();
    }

    // Form Fill Methods

    public void fillPatientForm(PatientData patient) {
        driver.findElement(firstNameInput).sendKeys(patient.firstName);
        driver.findElement(lastNameInput).sendKeys(patient.lastName);
        driver.findElement(dateOfBirthInput).sendKeys(patient.dateOfBirth);

        new Select(driver.findElement(genderSelect)).selectByVisibleText(patient.gender);

        if (patient.phone != null) {
            driver.findElement(phoneInput).sendKeys(patient.phone);
        }
        if (patient.email != null) {
            driver.findElement(emailInput).sendKeys(patient.email);
        }
        if (patient.address != null) {
            driver.findElement(addressInput).sendKeys(patient.address);
        }

        driver.findElement(insuranceNumberInput).sendKeys(patient.insuranceNumber);

        if (patient.bloodType != null) {
            new Select(driver.findElement(bloodTypeSelect))
                    .selectByVisibleText(patient.bloodType);
        }
        if (patient.allergies != null) {
            driver.findElement(allergiesInput).sendKeys(patient.allergies);
        }
        if (patient.emergencyContactName != null) {
            driver.findElement(emergencyContactNameInput)
                    .sendKeys(patient.emergencyContactName);
        }
        if (patient.emergencyContactPhone != null) {
            driver.findElement(emergencyContactPhoneInput)
                    .sendKeys(patient.emergencyContactPhone);
        }

        new Select(driver.findElement(statusSelect)).selectByValue(patient.status);

        WebElement epaCheckbox = driver.findElement(epaEnabledCheckbox);
        if (patient.epaEnabled != epaCheckbox.isSelected()) {
            epaCheckbox.click();
        }
    }

    public void submitForm() {
        driver.findElement(submitButton).click();
    }

    // Dialog Methods

    public boolean isDialogOpen() {
        try {
            String className = driver.findElement(dialogModal).getAttribute("class");
            return className != null && className.contains("active");
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public void waitForDialog() {
        wait.until(ExpectedConditions.attributeContains(dialogModal, "class", "active"));
    }

    public String getDialogTitle() {
        return driver.findElement(dialogTitle).getText();
    }

    public String getDialogBody() {
        return driver.findElement(dialogBody).getText();
    }

    public void clickDialogOk() {
        wait.until(ExpectedConditions.elementToBeClickable(dialogOkButton)).click();
    }

    public void clickDialogCancel() {
        driver.findElement(dialogCancelButton).click();
    }

    // Table Methods

    public boolean isTableVisible() {
        try {
            return driver.findElement(patientsTable).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public int getPatientRowCount() {
        return driver.findElements(tableRows).size();
    }

    public void clickEditOnRow(int rowIndex) {
        List<WebElement> rows = driver.findElements(tableRows);
        WebElement editButton = rows.get(rowIndex)
                .findElement(By.xpath(".//button[@title='Patient bearbeiten']"));
        editButton.click();
    }

    public void clickSyncOnRow(int rowIndex) {
        List<WebElement> rows = driver.findElements(tableRows);
        WebElement syncButton = rows.get(rowIndex)
                .findElement(By.xpath(".//button[@title='Zur EPA synchronisieren']"));
        syncButton.click();
    }

    public void clickDeleteOnRow(int rowIndex) {
        List<WebElement> rows = driver.findElements(tableRows);
        WebElement deleteButton = rows.get(rowIndex)
                .findElement(By.xpath(".//button[@title='Patient löschen']"));
        deleteButton.click();
    }

    public String getPatientNameFromRow(int rowIndex) {
        List<WebElement> rows = driver.findElements(tableRows);
        return rows.get(rowIndex).findElement(By.xpath(".//td[2]")).getText();
    }

    // EPA Status Methods

    public String getEPAStatusText() {
        return driver.findElement(epaStatusText).getText();
    }

    public boolean isEPAConnected() {
        String className = driver.findElement(epaStatusBanner).getAttribute("class");
        return className != null && !className.contains("disconnected");
    }

    public boolean areStatsVisible() {
        try {
            return !driver.findElement(statsContainer).getText().isEmpty();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    // JavaScript Function Calls

    public Object callJavaScriptFunction(String functionName, Object... args) {
        StringBuilder script = new StringBuilder("return " + functionName + "(");
        for (int i = 0; i < args.length; i++) {
            if (i > 0)
                script.append(", ");
            if (args[i] instanceof String) {
                script.append("'").append(args[i]).append("'");
            } else {
                script.append(args[i]);
            }
        }
        script.append(");");
        return js.executeScript(script.toString());
    }

    public void callAsyncJavaScriptFunction(String functionName, Object... args) {
        StringBuilder script = new StringBuilder(functionName + "(");
        for (int i = 0; i < args.length; i++) {
            if (i > 0)
                script.append(", ");
            if (args[i] instanceof String) {
                script.append("'").append(args[i]).append("'");
            } else {
                script.append(args[i]);
            }
        }
        script.append(");");
        js.executeAsyncScript(script.toString());
    }

    public Object getJavaScriptVariable(String variableName) {
        return js.executeScript("return " + variableName + ";");
    }

    // Helper class for patient data
    public static class PatientData {
        public String firstName;
        public String lastName;
        public String dateOfBirth;
        public String gender;
        public String phone;
        public String email;
        public String address;
        public String insuranceNumber;
        public String bloodType;
        public String allergies;
        public String emergencyContactName;
        public String emergencyContactPhone;
        public String status;
        public boolean epaEnabled;

        public static PatientData createDefault() {
            PatientData data = new PatientData();
            data.firstName = "Test";
            data.lastName = "Patient";
            data.dateOfBirth = "1990-01-01";
            data.gender = "Männlich";
            data.phone = "0221123456";
            data.email = "test@example.com";
            data.address = "Teststraße 1, 50667 Köln";
            data.insuranceNumber = "TEST-" + System.currentTimeMillis();
            data.bloodType = "A+";
            data.allergies = "Keine";
            data.emergencyContactName = "Emergency Contact";
            data.emergencyContactPhone = "0221999888";
            data.status = "active";
            data.epaEnabled = true;
            return data;
        }
    }
}