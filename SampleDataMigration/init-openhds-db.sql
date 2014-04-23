DROP USER 'openhds'@'localhost';
CREATE USER 'openhds'@'localhost' IDENTIFIED BY 'openhds';
DROP DATABASE `openhds`;
CREATE DATABASE `openhds`;
GRANT ALL PRIVILEGES ON openhds.* TO 'openhds'@'localhost';
FLUSH PRIVILEGES;
