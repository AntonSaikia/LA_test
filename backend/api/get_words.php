<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json");

// MySQL connection details from environment variables
$servername = getenv('DB_HOST') ?: "localhost"; // Default for local XAMPP
$username = getenv('DB_USER') ?: "root";
$password = getenv('DB_PASSWORD') ?: ""; // Default for local XAMPP
$dbname = getenv('DB_NAME') ?: "eng_deu_vocab";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    error_log("Database connection failed: " . $conn->connect_error);
    http_response_code(500); // Send an internal server error status
    echo json_encode(array("message" => "Database connection error."));
    exit();
}

// Query to fetch data
$sql = "SELECT * FROM vocabulary ORDER BY RAND() LIMIT 1";

$result = $conn->query($sql);

if ($result->num_rows > 0) {
    $row = $result->fetch_assoc();
    echo json_encode($row);
} else {
    echo json_encode(array("message" => "No data found"));
}

// Close connection
$conn->close();
?>
