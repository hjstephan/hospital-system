package com.hospital.ui;

import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

public class BaseUITest {

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected JavascriptExecutor js;
    protected static final String BASE_URL = "http://localhost:8080/hospital-management";
    protected static final int DEFAULT_TIMEOUT = 10;

    protected static final Logger LOGGER = Logger.getLogger(BaseUITest.class.getName());

    @BeforeAll
    public static void setUpClass() {
        // Automatically download and setup ChromeDriver
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    public void setUp() {
        ChromeOptions options = new ChromeOptions();

        // Headless mode for CI/CD (comment out for debugging)
        // options.addArguments("--headless=new");

        // Performance and stability options
        options.addArguments(
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--window-size=1920,1080",
                "--disable-blink-features=AutomationControlled",
                "--disable-notifications");

        // Set logging level (Java 21 - use var)
        var loggingPrefs = java.util.Map.of("browser", "ALL");
        options.setCapability("goog:loggingPrefs", loggingPrefs);

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));
        js = (JavascriptExecutor) driver;

        // Navigate to application
        driver.get(BASE_URL);

        // Wait for page to be fully loaded
        waitForPageLoad();

        LOGGER.info("Test setup complete. Browser opened at: " + BASE_URL);
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            // Capture console logs on failure
            try {
                var logs = driver.manage().logs().get("browser");
                if (!logs.getAll().isEmpty()) {
                    LOGGER.info("Browser console logs:");
                    logs.forEach(entry -> LOGGER.log(Level.INFO, entry.getMessage()));
                }
            } catch (Exception e) {
                LOGGER.warning("Could not retrieve browser logs: " + e.getMessage());
            }

            driver.quit();
            LOGGER.info("Browser closed");
        }
    }

    // Helper Methods

    protected void waitForPageLoad() {
        wait.until(driver -> js.executeScript("return document.readyState").equals("complete"));
    }

    protected Object executeScript(String script, Object... args) {
        return js.executeScript(script, args);
    }

    protected Object executeAsyncScript(String script, Object... args) {
        return js.executeAsyncScript(script, args);
    }

    protected void waitForMillis(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected boolean waitForCondition(java.util.function.Supplier<Boolean> condition,
            int timeoutSeconds) {
        long endTime = System.currentTimeMillis() + (timeoutSeconds * 1000L);
        while (System.currentTimeMillis() < endTime) {
            if (condition.get()) {
                return true;
            }
            waitForMillis(100);
        }
        return false;
    }

    protected void logJavaScriptVariable(String variableName) {
        Object value = executeScript("return " + variableName);
        LOGGER.info(variableName + " = " + value);
    }
}