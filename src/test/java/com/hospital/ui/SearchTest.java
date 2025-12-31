package com.hospital.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.hospital.pages.PatientManagementPage;

public class SearchTest extends BaseUITest {

    @Test
    public void testSearchWithDebounce() {
        PatientManagementPage page = new PatientManagementPage(driver);

        // Enter search query
        page.enterSearchQuery("Max");

        // Wait for debounce (400ms) + search to complete
        waitForMillis(1000);

        // Verify search was executed
        String searchQuery = (String) executeScript("return currentSearchQuery;");
        assertEquals("Max", searchQuery);

        // Verify offset reset
        Long offset = ((Number) executeScript("return searchOffset;")).longValue();
        assertEquals(0L, offset);
    }

    @Test
    public void testSearchMinimumCharacters() {
        PatientManagementPage page = new PatientManagementPage(driver);

        // Enter only 1 character (should not trigger search)
        page.enterSearchQuery("M");
        waitForMillis(500);

        // currentSearchQuery should still be empty
        String searchQuery = (String) executeScript("return currentSearchQuery;");
        assertTrue(searchQuery == null || searchQuery.isEmpty());

        // Add second character (should trigger search)
        page.enterSearchQuery("Ma");
        waitForMillis(1000);

        searchQuery = (String) executeScript("return currentSearchQuery;");
        assertEquals("Ma", searchQuery);
    }

    @Test
    public void testLoadPatientsFunction() {
        // Test loadPatients with empty filter (random)
        executeAsyncScript(
                "var callback = arguments[arguments.length - 1];" +
                        "isInitialLoad = true;" +
                        "loadPatients('').then(() => callback(true));");

        waitForMillis(2000);

        PatientManagementPage page = new PatientManagementPage(driver);
        assertTrue(page.isTableVisible());

        Boolean isInitialLoad = (Boolean) executeScript("return isInitialLoad;");
        assertFalse(isInitialLoad);
    }

    @Test
    public void testLoadPatientsWithFilter() {
        // Test loadPatients with filter
        executeAsyncScript(
                "var callback = arguments[arguments.length - 1];" +
                        "loadPatients('Max').then(() => callback(true));");

        waitForMillis(2000);

        String searchQuery = (String) executeScript("return currentSearchQuery;");
        assertEquals("Max", searchQuery);

        Long offset = ((Number) executeScript("return searchOffset;")).longValue();
        assertEquals(0L, offset);
    }

    @Test
    public void testLoadMorePatients() {
        PatientManagementPage page = new PatientManagementPage(driver);

        // Do a search that should have many results
        page.enterSearchQuery("M");
        waitForMillis(1500);

        // Get initial count
        int initialCount = page.getPatientRowCount();

        // Check if hasMoreResults
        Boolean hasMore = (Boolean) executeScript("return hasMoreResults;");

        if (hasMore != null && hasMore) {
            // Call loadMorePatients
            executeAsyncScript(
                    "var callback = arguments[arguments.length - 1];" +
                            "loadMorePatients().then(() => callback(true));");

            waitForMillis(2000);

            // Verify offset increased
            Long offset = ((Number) executeScript("return searchOffset;")).longValue();
            assertTrue(offset > 0);

            // Verify more rows loaded
            int newCount = page.getPatientRowCount();
            assertTrue(newCount >= initialCount);
        }
    }

    @Test
    public void testRefreshPatients() {
        PatientManagementPage page = new PatientManagementPage(driver);

        // Do a search first
        page.enterSearchQuery("Test");
        waitForMillis(1000);

        // Click refresh
        page.clickRefresh();
        waitForMillis(1000);

        // Should reset everything
        String searchValue = page.getSearchValue();
        assertTrue(searchValue.isEmpty());

        Boolean isInitialLoad = (Boolean) executeScript("return isInitialLoad;");
        assertFalse(isInitialLoad);

        String searchQuery = (String) executeScript("return currentSearchQuery;");
        assertTrue(searchQuery == null || searchQuery.isEmpty());
    }
}