PRAGMA user_version = 14
DROP TABLE IF EXISTS individuals
DROP TABLE IF EXISTS locations
DROP TABLE IF EXISTS hierarchyitems
DROP TABLE IF EXISTS fieldworkers
CREATE TABLE IF NOT EXISTS android_metadata (locale TEXT)
CREATE TABLE IF NOT EXISTS individuals (_id INTEGER,uuid TEXT PRIMARY KEY NOT NULL,dob TEXT,extId TEXT,firstName TEXT,gender TEXT,lastName TEXT,currentResidence TEXT,relationshipToHead TEXT,otherNames TEXT,phoneNumber TEXT,otherPhoneNumber TEXT,pointOfContactName TEXT,pointOfContactPhoneNumber TEXT,languagePreference TEXT,status TEXT,nationality TEXT,otherId TEXT, attrs TEXT)
CREATE TABLE IF NOT EXISTS locations (_id INTEGER,extId TEXT NOT NULL,uuid TEXT NOT NULL PRIMARY KEY,hierarchyUuid TEXT NOT NULL,hierarchyExtId TEXT,latitude TEXT,longitude TEXT,communityName TEXT,communityCode TEXT,localityName TEXT,mapAreaName TEXT,sectorName INT,buildingNumber INT,floorNumber TEXT,regionName TEXT,provinceName TEXT,subDistrictName TEXT,districtName TEXT,hasRecievedBedNets TEXT,sprayingEvaluation TEXT,description TEXT,evaluationStatus TEXT,name TEXT NOT NULL, attrs TEXT)
CREATE TABLE IF NOT EXISTS hierarchyitems (_id INTEGER,uuid TEXT NOT NULL PRIMARY KEY,extId TEXT NOT NULL,level TEXT NOT NULL,name TEXT NOT NULL,parent TEXT NOT NULL, attrs TEXT)
CREATE TABLE IF NOT EXISTS fieldworkers (_id INTEGER,uuid TEXT PRIMARY KEY NOT NULL,extId TEXT NOT NULL,idPrefix TEXT NOT NULL,firstName TEXT NOT NULL,lastName TEXT NOT NULL,password TEXT NOT NULL)