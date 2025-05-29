package com.example.latest;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import org.junit.jupiter.api.*;
import java.util.regex.Pattern;
import java.util.List;
import java.util.Map; // Import Map for passing arguments

public class FrontendTests {
    static Playwright playwright;
    static Browser browser;

    // New: Declare a Page object that can be accessed by all tests
    Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        // FIX 1: Change playwright.chromium to playwright.chromium()
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
    }

    @AfterAll
    static void closeBrowser() {
        playwright.close();
    }

    @BeforeEach
    void createContextAndPage() {
        // Use browser.newPage() to create a new page for each test
        page = browser.newPage();
        // Base URL for the application
        page.navigate("http://localhost/frontend");
    }

    @AfterEach
    void closePage() {
        page.close();
    }

    @Test
    void testPageTitle() {
        assertThat(page).hasTitle(Pattern.compile("Language App"));
    }

    @Test
    void testInitialWordDisplay() {
        // Assert that the English word "Hello" is displayed
        assertThat(page.locator("div.word-pair p.english-word")).hasText("Hello");
        // Assert that the German word "Hallo" is displayed
        assertThat(page.locator("div.word-pair p.german-word")).hasText("Hallo");
    }

    @Test
    void testNextWordButton() {
        // Get the initial English word
        String initialEnglishWord = page.locator("div.word-pair p.english-word").textContent();

        // Click the "Next Word" button
        page.click("button:has-text('Next Word')");

        // Wait for the English word to change
        // Using waitForFunction to wait until the text content is different from the initial word
        // Pass a Map as the argument for the JavaScript function
        page.waitForFunction("({ selector, initialText }) => {" +
                             "  const element = document.querySelector(selector);" +
                             "  return element && element.textContent !== initialText;" +
                             "}",
                             Map.of("selector", "div.word-pair p.english-word", "initialText", initialEnglishWord));


        // FIX 2: Change doesNotHaveText to not().hasText()
        assertThat(page.locator("div.word-pair p.english-word")).not().hasText(initialEnglishWord);
    }

    @Test
    void testShowAnswerButton() {
        // Ensure the German word is initially hidden or not fully visible
        assertThat(page.locator("div.word-pair p.german-word")).isHidden();

        // Click the "Show Answer" button
        page.click("button:has-text('Show Answer')");

        // Assert that the German word is now visible
        assertThat(page.locator("div.word-pair p.german-word")).isVisible();
    }

    @Test
    void testCombinedFunctionality() {
        // Verify initial state
        assertThat(page.locator("div.word-pair p.english-word")).hasText("Hello");
        assertThat(page.locator("div.word-pair p.german-word")).isHidden();

        // Click "Show Answer"
        page.click("button:has-text('Show Answer')");
        assertThat(page.locator("div.word-pair p.german-word")).isVisible();
        assertThat(page.locator("div.word-pair p.german-word")).hasText("Hallo");

        // Store current English word
        String currentEnglishWord = page.locator("div.word-pair p.english-word").textContent();

        // Click "Next Word"
        page.click("button:has-text('Next Word')");

        // Wait for English word to change, and German word to become hidden again
        page.waitForFunction("({ englishSelector, germanSelector, initialEnglishText }) => {" +
                             "  const englishWordElement = document.querySelector(englishSelector);" +
                             "  const germanWordElement = document.querySelector(germanSelector);" +
                             "  return englishWordElement && englishWordElement.textContent !== initialEnglishText &&" +
                             "         germanWordElement && getComputedStyle(germanWordElement).visibility === 'hidden';" +
                             "}",
                             Map.of("englishSelector", "div.word-pair p.english-word",
                                    "germanSelector", "div.word-pair p.german-word",
                                    "initialEnglishText", currentEnglishWord));


        // FIX 3: Change doesNotHaveText to not().hasText()
        assertThat(page.locator("div.word-pair p.english-word")).not().hasText(currentEnglishWord);
        assertThat(page.locator("div.word-pair p.german-word")).isHidden();
    }

    @Test
    void testWordProgression() {
        // Assuming there are at least 3 words: Hello, World, Goodbye
        String[] expectedEnglishWords = {"Hello", "World", "Goodbye"};

        for (int i = 0; i < expectedEnglishWords.length; i++) {
            String initialEnglishWord = page.locator("div.word-pair p.english-word").textContent();
            assertThat(page.locator("div.word-pair p.english-word")).hasText(expectedEnglishWords[i]);

            if (i < expectedEnglishWords.length - 1) {
                // Click next word
                page.click("button:has-text('Next Word')");
                // Wait for the word to change
                page.waitForFunction("({ selector, initialText }) => {" +
                                     "  const element = document.querySelector(selector);" +
                                     "  return element && element.textContent !== initialText;" +
                                     "}",
                                     Map.of("selector", "div.word-pair p.english-word", "initialText", initialEnglishWord));
            }
        }
    }

    @Test
    void testElementVisibilityOnLoad() {
        // Assert that the "Next Word" button is visible
        assertThat(page.locator("button:has-text('Next Word')")).isVisible();
        // Assert that the "Show Answer" button is visible
        assertThat(page.locator("button:has-text('Show Answer')")).isVisible();
        // Assert that the English word element is visible
        assertThat(page.locator("div.word-pair p.english-word")).isVisible();
    }
}