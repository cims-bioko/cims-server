DROP USER 'sample'@'localhost';
CREATE USER 'sample'@'localhost' IDENTIFIED BY 'sample';
DROP DATABASE `sample`;
CREATE DATABASE `sample`;
GRANT ALL PRIVILEGES ON sample.* TO 'sample'@'localhost';
FLUSH PRIVILEGES;
