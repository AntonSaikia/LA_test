-- database/schema.sql
CREATE DATABASE IF NOT EXISTS eng_deu_vocab;
USE eng_deu_vocab;

CREATE TABLE IF NOT EXISTS `vocabulary` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `english_word` VARCHAR(255) NOT NULL,
  `german_word` VARCHAR(255) NOT NULL
);
