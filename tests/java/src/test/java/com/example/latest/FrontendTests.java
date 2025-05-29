package com.example.latest;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.Locator; // Import Locator for Locator.WaitForOptions
import com.microsoft.playwright.assertions.PlaywrightAssertions; // Correct import for Playwright assertions
import org.junit.jupiter.api.*;
import java.nio.file.Paths;
// We don't need java.util.regex.Pattern if we're doing an exact title match.
// If you want to use regex for title, keep this import and adjust the testPageTitle assertion.

public class FrontendTests {
    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        // Correct way to access chromium
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true)); // Use headless for CI
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeEach
    void createContextAndPage() {
        context = browser.newContext();
        // Start tracing before each test for better debugging on CI
        context.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true));
        page = context.newPage();
        // Navigate to the correct base URL
        page.navigate("http://localhost/frontend/");
        // Add a wait for the main content to be loaded/visible
        // This is crucial because the words are fetched via API after page load
        page.waitForSelector("div.word-pair p.english-word", new Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE));
    }

    @AfterEach
    void closeContext() {
        // Stop tracing after each test and save to a unique file
        context.tracing().stop(new Tracing.StopOptions().setPath(Paths.get("trace-" + System.currentTimeMillis() + ".zip")));
        if (context != null) {
            context.close();
        }
    }

    @Test
    void testPageTitle() {
        // Correct the expected title based on index.html
        PlaywrightAssertions.assertThat(page).hasTitle("German Words");
    }

    @Test
    void testInitialWordDisplay() {
        // The element exists but is initially empty, content loads via JS
        // We already added page.waitForSelector in @BeforeEach, so it should be visible now.
        // Get the actual text from the element after it's loaded.
        String initialEnglishWord = page.locator("div.word-pair p.english-word").textContent();
        // Assert that it's not empty and matches an expected initial word from your seed data
        // (Assuming "Hello" is indeed the first word from your database based on seed_data.sql)
        PlaywrightAssertions.assertThat(page.locator("div.word-pair p.english-word")).hasText("Hello");
    }

    @Test
    void testNextWordButton() {
        // Ensure the English word is loaded before interacting
        page.waitForSelector("div.word-pair p.english-word", new Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE));
        String initialEnglishWord = page.locator("div.word-pair p.english-word").textContent();

        // Click the 'Next Word' button
        page.locator("button:has-text('Next Word')").click();

        // Wait for the text to change (meaning a new word has loaded)
        // This is a more robust way to wait for dynamic content change
        // Correct usage of Locator.WaitForOptions
        page.locator("div.word-pair p.english-word").waitFor(new Locator.WaitForOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE));
        PlaywrightAssertions.assertThat(page.locator("div.word-pair p.english-word")).isNotEqualTo(initialEnglishWord);
    }

    @Test
    void testShowAnswerButton() {
        // Ensure the English word is loaded before interacting
        page.waitForSelector("div.word-pair p.english-word", new Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE));

        // Click the 'Show Answer' button
        page.locator("button:has-text('Show Answer')").click();

        // Check that the German word is now visible (it was likely empty or hidden before)
        PlaywrightAssertions.assertThat(page.locator("div.word-pair p.german-word")).isVisible();
        // Assert the German word has some text (not empty)
        PlaywrightAssertions.assertThat(page.locator("div.word-pair p.german-word")).not().hasText("");
    }

    @Test
    void testCombinedFunctionality() {
        // Ensure initial word is loaded
        page.waitForSelector("div.word-pair p.english-word", new Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE));
        PlaywrightAssertions.assertThat(page.locator("div.word-pair p.english-word")).hasText("Hello");

        // Click Show Answer
        page.locator("button:has-text('Show Answer')").click();
        PlaywrightAssertions.assertThat(page.locator("div.word-pair p.german-word")).isVisible();
        PlaywrightAssertions.assertThat(page.locator("div.word-pair p.german-word")).hasText("Hallo"); // Assuming "Hallo" is the German translation for "Hello" in your seed data

        // Click Next Word
        page.locator("button:has-text('Next Word')").click();
        // Wait for the English word to change
        page.locator("div.word-pair p.english-word").waitFor(new Locator.WaitForOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE));
        PlaywrightAssertions.assertThat(page.locator("div.word-pair p.english-word")).isNotEqualTo("Hello");
        PlaywrightAssertions.assertThat(page.locator("div.word-pair p.german-word")).not().isVisible(); // German word should be hidden again
    }

    @Test
    void testWordProgression() {
        // Ensure initial word is loaded
        page.waitForSelector("div.word-pair p.english-word", new Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE));

        String firstWord = page.locator("div.word-pair p.english-word").textContent();
        page.locator("button:has-text('Next Word')").click();

        // Wait for the word to change
        page.locator("div.word-pair p.english-word").waitFor(new Locator.WaitForOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE));
        String secondWord = page.locator("div.word-pair p.english-word").textContent();
        PlaywrightAssertions.assertThat(secondWord).isNotEqualTo(firstWord);

        page.locator("button:has-text('Next Word')").click();

        // Wait for the word to change again
        page.locator("div.word-pair p.english-word").waitFor(new Locator.WaitForOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE));
        String thirdWord = page.locator("div.word-pair p.english-word").textContent();
        PlaywrightAssertions.assertThat(thirdWord).isNotEqualTo(secondWord);
        PlaywrightAssertions.assertThat(thirdWord).isNotEqualTo(firstWord);
    }

    @Test
    void testElementVisibilityOnLoad() {
        // These buttons are always visible on load according to index.html
        PlaywrightAssertions.assertThat(page.locator("button:has-text('Show Answer')")).isVisible();
        PlaywrightAssertions.assertThat(page.locator("button:has-text('Next Word')")).isVisible();
    }
}