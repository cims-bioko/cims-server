PRAGMA user_version = 13
DROP TABLE IF EXISTS individuals
DROP TABLE IF EXISTS locations
DROP TABLE IF EXISTS hierarchyitems
DROP TABLE IF EXISTS visits
DROP TABLE IF EXISTS relationships
DROP TABLE IF EXISTS fieldworkers
DROP TABLE IF EXISTS socialgroups
DROP TABLE IF EXISTS memberships
CREATE TABLE IF NOT EXISTS android_metadata (locale TEXT)
CREATE TABLE IF NOT EXISTS individuals (_id INTEGER,uuid TEXT PRIMARY KEY NOT NULL,dob TEXT,extId TEXT,father TEXT,firstName TEXT,gender TEXT,lastName TEXT,mother TEXT,currentResidence TEXT,endType TEXT,otherNames TEXT,age TEXT,ageUnits TEXT,phoneNumber TEXT,otherPhoneNumber TEXT,pointOfContactName TEXT,pointOfContactPhoneNumber TEXT,languagePreference TEXT,memberStatus TEXT,nationality TEXT,otherId TEXT)
CREATE TABLE IF NOT EXISTS locations (_id INTEGER,extId TEXT NOT NULL,uuid TEXT NOT NULL PRIMARY KEY,hierarchyUuid TEXT NOT NULL,hierarchyExtId TEXT,latitude TEXT,longitude TEXT,communityName TEXT,communityCode TEXT,localityName TEXT,mapAreaName TEXT,sectorName INT,buildingNumber INT,floorNumber TEXT,regionName TEXT,provinceName TEXT,subDistrictName TEXT,districtName TEXT,hasRecievedBedNets TEXT,sprayingEvaluation TEXT,description TEXT,evaluationStatus TEXT,name TEXT NOT NULL)
CREATE TABLE IF NOT EXISTS hierarchyitems (_id INTEGER,uuid TEXT NOT NULL PRIMARY KEY,extId TEXT NOT NULL,level TEXT NOT NULL,name TEXT NOT NULL,parent TEXT NOT NULL)
CREATE TABLE IF NOT EXISTS visits (_id INTEGER,uuid TEXT NOT NULL PRIMARY KEY,date TEXT NOT NULL,extId TEXT NOT NULL,fieldWorkerUuid TEXT NOT NULL,location_uuid TEXT NOT NULL)
CREATE TABLE IF NOT EXISTS relationships (_id INTEGER,uuid TEXT NOT NULL PRIMARY KEY,individualA TEXT NOT NULL,individualB TEXT NOT NULL,relationshipType TEXT NOT NULL,startDate TEXT NOT NULL)
CREATE TABLE IF NOT EXISTS fieldworkers (_id INTEGER,uuid TEXT PRIMARY KEY NOT NULL,extId TEXT NOT NULL,idPrefix TEXT NOT NULL,firstName TEXT NOT NULL,lastName TEXT NOT NULL,password TEXT NOT NULL)
CREATE TABLE IF NOT EXISTS socialgroups (_id INTEGER,location_uuid TEXT NOT NULL,uuid TEXT NOT NULL PRIMARY KEY,groupHead_uuid TEXT NOT NULL,groupName TEXT NOT NULL)
CREATE TABLE IF NOT EXISTS memberships (_id INTEGER,uuid TEXT NOT NULL PRIMARY KEY,individual_uuid TEXT NOT NULL,socialGroup_uuid TEXT NOT NULL,relationshipToHead TEXT NOT NULL)
