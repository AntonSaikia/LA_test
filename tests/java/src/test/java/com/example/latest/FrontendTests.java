package com.example.latest; // Updated package name

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class FrontendTests { // Updated class name

    static Playwright playwright;
    static Browser browser;
    Page page;

    // Base paths for frontend and API within the CI environment (Apache paths)
    private final String FRONTEND_BASE_PATH = System.getProperty("frontend.base.path", "/frontend/");
    private final String API_BASE_PATH = System.getProperty("api.base.path", "/api/"); // Updated API path

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
    }

    @AfterAll
    static void closeBrowser() {
        playwright.close();
    }

    @BeforeEach
    void createContextAndPage() {
        BrowserContext context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void closeContext() {
        if (page != null) {
            page.context().close();
        }
    }

    @Test
    @DisplayName("Should display word correctly when fetched successfully")
    void testSuccessfulWordFetchAndDisplay() {
        page.navigate("http://localhost" + FRONTEND_BASE_PATH, new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));

        Locator englishWord = page.locator("data-testid=english-word");
        Locator germanWord = page.locator("data-testid=german-word");

        assertTrue(englishWord.isVisible(), "English word element should be visible");
        assertTrue(germanWord.isVisible(), "German word element should be visible");
        assertTrue(!englishWord.textContent().isEmpty(), "English word should not be empty");
        assertTrue(!germanWord.textContent().isEmpty(), "German word should not be empty");

        System.out.println("Fetched English Word: " + englishWord.textContent());
        System.out.println("Fetched German Word: " + germanWord.textContent());
    }

    @Test
    @DisplayName("Should display 'No data found' message when API returns empty")
    void testNoDataFoundScenario() {
        // Intercept the API call and return the "No data found" message
        // Updated to new API path and filename: /api/get-word.php
        page.route("**/api/get-word.php", route -> {
            route.fulfill(new Route.FulfillOptions()
                    .setStatus(200)
                    .setContentType("application/json")
                    .setBody("{\"message\": \"No data found\"}"));
        });

        page.navigate("http://localhost" + FRONTEND_BASE_PATH, new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));

        Locator errorMessage = page.locator("data-testid=error-message");
        assertTrue(errorMessage.isVisible(), "Error message should be visible");
        assertTrue(errorMessage.textContent().contains("No data found"), "Error message text should contain 'No data found'");

        assertFalse(page.locator("data-testid=english-word").isVisible(), "English word should not be visible");
        assertFalse(page.locator("data-testid=german-word").isVisible(), "German word should not be visible");
    }

    @Test
    @DisplayName("Should display error message on network failure")
    void testNetworkErrorScenario() {
        // Intercept the API call and abort it to simulate network error
        // Updated to new API path and filename: /api/get-word.php
        page.route("**/api/get-word.php", Route::abort);

        page.navigate("http://localhost" + FRONTEND_BASE_PATH, new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));

        Locator errorMessage = page.locator("data-testid=error-message");
        assertTrue(errorMessage.isVisible(), "Error message should be visible on network failure");
        assertTrue(errorMessage.textContent().contains("Failed to load words"), "Error message text should contain 'Failed to load words'");

        assertFalse(page.locator("data-testid=english-word").isVisible(), "English word should not be visible");
        assertFalse(page.locator("data-testid=german-word").isVisible(), "German word should not be visible");
    }

    @Test
    @DisplayName("Should fetch a new word on refresh button click")
    void testRefreshFunctionality() {
        page.navigate("http://localhost" + FRONTEND_BASE_PATH, new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));

        Locator englishWordLocator = page.locator("data-testid=english-word");
        Locator germanWordLocator = page.locator("data-testid=german-word");
        Locator refreshButton = page.locator("data-testid=new-word-button");

        assertTrue(englishWordLocator.isVisible(), "Initial English word should be visible");
        assertTrue(germanWordLocator.isVisible(), "Initial German word should be visible");
        String initialEnglishWord = englishWordLocator.textContent();
        String initialGermanWord = germanWordLocator.textContent();

        refreshButton.click();

        // Wait for the new word to load, by waiting for the text to change
        page.waitForFunction("selector => document.querySelector(selector) && document.querySelector(selector).textContent !== arguments[0]",
                             englishWordLocator.selector(), initialEnglishWord);

        String newEnglishWord = englishWordLocator.textContent();
        String newGermanWord = germanWordLocator.textContent();

        System.out.println("Initial English: " + initialEnglishWord + ", New English: " + newEnglishWord);
        System.out.println("Initial German: " + initialGermanWord + ", New German: " + newGermanWord);

        assertNotEquals(initialEnglishWord, newEnglishWord, "English word should change after refresh");
        assertNotEquals(initialGermanWord, newGermanWord, "German word should change after refresh");
    }
}
