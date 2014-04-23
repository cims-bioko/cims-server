 -- I used LibreOffice to save "Master_DB_All_Houses - Round 19 v1 - bsh-formatting.xlsx"
 -- as a .csv text file.  I chose UTF-8 encoding.  I chose , field delimiter and " text delimiter.
 -- I selected "Save cell content as shown" and "Quote all text cells".
 -- I chose column names and types below by inspection and translating from Spanish.

CREATE TABLE sample_data
(
Site varchar(255),
Community varchar(255),
SprayArea varchar(255),
MapArea varchar(255),
UNIQUEBID varchar(255),
MapAreaAndSector varchar(255),
Sector int,
BuildingAndFloor varchar(255),
Floor int,
Latitude float,
Longitude float,
BuildingType int,
Description varchar(255),
HeadOfHousehold varchar(255),
Rooms int,
District varchar(255),
FECHA1 varchar(255),
FECHA2 varchar(255),
Province varchar(255),
PRIMARY KEY (UNIQUEBID)
);

 -- bulk add from csv to new table
 -- IGNORE duplicate UNIQUEBIDs
LOAD DATA LOCAL INFILE 'Master_DB_All_Houses - Round 19 v1 - bsh-formatting.csv' IGNORE INTO TABLE sample_data
  FIELDS TERMINATED BY ',' ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES;

 -- add default column values
UPDATE sample_data SET Province = 'UnknownProvince' WHERE Province IS NULL OR Province = '';
UPDATE sample_data SET District = 'UnknownDistrict' WHERE District IS NULL OR District = '';
UPDATE sample_data SET MapArea = 'UnknownMapArea' WHERE MapArea IS NULL OR MapArea = '';
UPDATE sample_data SET HeadOfHousehold = 'UnknownHeadOfHousehold' WHERE HeadOfHousehold IS NULL OR HeadOfHousehold = '' OR HeadOfHousehold = 'ND';

