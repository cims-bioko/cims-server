DROP USER 'openhds'@'localhost';
CREATE USER 'openhds'@'localhost' IDENTIFIED BY 'openhds';
DROP DATABASE `openhds`;
CREATE DATABASE `openhds`;
GRANT ALL PRIVILEGES ON *.* TO 'openhds'@'localhost';
GRANT ALL PRIVILEGES ON *.* TO 'openhds'@'130.111.%';
FLUSH PRIVILEGES;

