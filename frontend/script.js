// frontend/script.js

const wordContainer = document.getElementById('word-container');
const newWordButton = document.querySelector('[data-testid="new-word-button"]');

// IMPORTANT: Define API_BASE_URL dynamically based on environment
// For local XAMPP (files directly in htdocs): http://localhost
// For GitHub Actions (PHP mapped to /api/ in Apache container): http://php-apache/api
const API_BASE_URL = window.location.hostname === "localhost" ?
                     "http://localhost" :
                     "http://php-apache/api";


async function fetchWord() {
    wordContainer.innerHTML = '<div data-testid="loading-message" class="loading-message">Loading word...</div>';
    try {
        // Updated to new API path and filename: /api/get-word.php
        const response = await fetch(`${API_BASE_URL}/get-word.php`);
        const data = await response.json();

        if (response.ok) {
            if (data.message) {
                wordContainer.innerHTML = `<div data-testid="error-message" class="error-message">${data.message}</div>`;
            } else {
                wordContainer.innerHTML = `
                    <div class="word-display">
                        <p data-testid="english-word"><strong>English:</strong> ${data.english_word}</p>
                        <p data-testid="german-word"><strong>German:</strong> ${data.german_word}</p>
                    </div>
                `;
            }
        } else {
            wordContainer.innerHTML = `<div data-testid="error-message" class="error-message">Failed to fetch word. Server error.</div>`;
        }
    } catch (err) {
        console.error('Network or parsing error:', err);
        wordContainer.innerHTML = `<div data-testid="error-message" class="error-message">Failed to load words. Please check your connection.</div>`;
    }
}

// Initial fetch when the page loads
fetchWord();

// Attach event listener for the button
newWordButton.addEventListener('click', fetchWord);
