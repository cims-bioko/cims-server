
-- province_temp: distinct Province gives children of Bioko Region
--  Province level = e8033d20c5ac11e39c1a0800200c9a66
--  parent = Bioko = 36ec15f0c5ae11e39c1a0800200c9a66
drop table if exists sample.province_temp;
create table sample.province_temp (
uuid varchar(32), 
insertDate date, 
status varchar(255) default 'A', 
extId varchar(255), 
name varchar(255), 
level_uuid varchar(32), 
parent_uuid varchar(32), 
insertBy_uuid varchar(32) default 'e67f2870c59c11e39c1a0800200c9a66', 
collectedBy_uuid varchar(32) default '4786a2b0c59d11e39c1a0800200c9a66',
District varchar(255), 
SprayArea varchar(255), 
MapArea varchar(255), 
Sector varchar(255), 
MapAreaSector varchar(255),
Province varchar(255)
);

insert into province_temp (Province) 
SELECT DISTINCT Province FROM sample.sample_data
Where Province != 'UnknownProvince';
UPDATE province_temp SET uuid = replace(uuid(), '-', '');
UPDATE province_temp SET insertDate = date(now());
UPDATE province_temp SET extId = Province;
UPDATE province_temp SET name = Province;
UPDATE province_temp SET level_uuid = 'e8033d20c5ac11e39c1a0800200c9a66';
UPDATE province_temp SET parent_uuid = '36ec15f0c5ae11e39c1a0800200c9a66';

INSERT INTO openhds.locationhierarchy (uuid,name,extId,level_uuid,parent_uuid,insertDate,collectedBy_uuid,insertBy_uuid,status)
SELECT uuid,name,extId,level_uuid,parent_uuid,insertDate,collectedBy_uuid,insertBy_uuid,status FROM province_temp;

-- district_temp: distinct Province, District gives children of Provinces
--  District level = e8033d21c5ac11e39c1a0800200c9a66
--  parent = match on Province
drop table if exists sample.district_temp;
create table sample.district_temp (
uuid varchar(32), 
insertDate date, 
status varchar(255) default 'A', 
extId varchar(255), 
name varchar(255), 
level_uuid varchar(32), 
parent_uuid varchar(32), 
insertBy_uuid varchar(32) default 'e67f2870c59c11e39c1a0800200c9a66', 
collectedBy_uuid varchar(32) default '4786a2b0c59d11e39c1a0800200c9a66',
District varchar(255), 
SprayArea varchar(255), 
MapArea varchar(255), 
Sector varchar(255), 
MapAreaSector varchar(255),
Province varchar(255)
);

insert into district_temp (Province, District) 
SELECT DISTINCT Province, District FROM sample.sample_data
Where Province != 'UnknownProvince'
And District != 'UnknownDistrict';
UPDATE district_temp SET uuid = replace(uuid(), '-', '');
UPDATE district_temp SET insertDate = date(now());
UPDATE district_temp SET extId = District;
UPDATE district_temp SET name = District;
UPDATE district_temp SET level_uuid = 'e8033d21c5ac11e39c1a0800200c9a66';
UPDATE district_temp, province_temp SET district_temp.parent_uuid = province_temp.uuid 
WHERE province_temp.Province = district_temp.Province;

INSERT INTO openhds.locationhierarchy (uuid,name,extId,level_uuid,parent_uuid,insertDate,collectedBy_uuid,insertBy_uuid,status)
SELECT uuid,name,extId,level_uuid,parent_uuid,insertDate,collectedBy_uuid,insertBy_uuid,status FROM district_temp;

-- map_area_temp: distinct Province, District, MapArea gives children of Districts
--  MapArea level = e8033d22c5ac11e39c1a0800200c9a66
--  parent = match on Province, District
drop table if exists sample.map_area_temp;
create table sample.map_area_temp (
uuid varchar(32), 
insertDate date, 
status varchar(255) default 'A', 
extId varchar(255), 
name varchar(255), 
level_uuid varchar(32), 
parent_uuid varchar(32), 
insertBy_uuid varchar(32) default 'e67f2870c59c11e39c1a0800200c9a66', 
collectedBy_uuid varchar(32) default '4786a2b0c59d11e39c1a0800200c9a66',
District varchar(255), 
SprayArea varchar(255), 
MapArea varchar(255), 
Sector varchar(255), 
MapAreaSector varchar(255),
Province varchar(255)
);

insert into map_area_temp (Province, District, MapArea) 
SELECT DISTINCT Province, District, MapArea FROM sample.sample_data
Where Province != 'UnknownProvince'
And District != 'UnknownDistrict'
And MapArea != 'UnknownMapArea';
UPDATE map_area_temp SET uuid = replace(uuid(), '-', '');
UPDATE map_area_temp SET insertDate = date(now());
UPDATE map_area_temp SET extId = MapArea;
UPDATE map_area_temp SET name = MapArea;
UPDATE map_area_temp SET level_uuid = 'e8033d22c5ac11e39c1a0800200c9a66';
UPDATE map_area_temp, district_temp SET map_area_temp.parent_uuid = district_temp.uuid 
WHERE district_temp.Province = map_area_temp.Province
AND district_temp.District = map_area_temp.District;

INSERT INTO openhds.locationhierarchy (uuid,name,extId,level_uuid,parent_uuid,insertDate,collectedBy_uuid,insertBy_uuid,status)
SELECT uuid,name,extId,level_uuid,parent_uuid,insertDate,collectedBy_uuid,insertBy_uuid,status FROM map_area_temp;

-- sector_temp: distinct Province, District, MapArea, Sector gives children of MapAreas
--  Sector level = e8033d23c5ac11e39c1a0800200c9a66
--  parent = match on Province, District, MapArea
drop table if exists sample.sector_temp;
create table sample.sector_temp (
uuid varchar(32), 
insertDate date, 
status varchar(255) default 'A', 
extId varchar(255), 
name varchar(255), 
level_uuid varchar(32), 
parent_uuid varchar(32), 
insertBy_uuid varchar(32) default 'e67f2870c59c11e39c1a0800200c9a66', 
collectedBy_uuid varchar(32) default '4786a2b0c59d11e39c1a0800200c9a66',
District varchar(255), 
SprayArea varchar(255), 
MapArea varchar(255), 
Sector varchar(255), 
MapAreaSector varchar(255),
Province varchar(255)
);

insert into sector_temp (Province, District, MapArea, Sector, MapAreaSector) 
SELECT DISTINCT Province, District, MapArea, Sector, MapAreaAndSector FROM sample.sample_data
Where Province != 'UnknownProvince'
And District != 'UnknownDistrict'
And MapArea != 'UnknownMapArea'
And Sector IS NOT NULL;
UPDATE sector_temp SET uuid = replace(uuid(), '-', '');
UPDATE sector_temp SET insertDate = date(now());
UPDATE sector_temp SET name = lpad(format(Sector,0), 4, 'S000');
UPDATE sector_temp SET extId = MapAreaSector;
UPDATE sector_temp SET level_uuid = 'e8033d23c5ac11e39c1a0800200c9a66';
UPDATE sector_temp, map_area_temp SET sector_temp.parent_uuid = map_area_temp.uuid 
WHERE map_area_temp.Province = sector_temp.Province
AND map_area_temp.District = sector_temp.District
AND map_area_temp.MapArea = sector_temp.MapArea;

INSERT INTO openhds.locationhierarchy (uuid,name,extId,level_uuid,parent_uuid,insertDate,collectedBy_uuid,insertBy_uuid,status)
SELECT uuid,name,extId,level_uuid,parent_uuid,insertDate,collectedBy_uuid,insertBy_uuid,status FROM sector_temp;

-- location_temp: distinct District, Site, SprayArea, UNIQUEBID, Latitude, Longitude, BuildingType gives locations
--  location hierarchy = match Province, District, MapArea, Sector
drop table if exists sample.location_temp;
create table sample.location_temp (
uuid varchar(32), 
extId varchar(255), 
locationName varchar(255), 
locationLevel_uuid varchar(32), 
locationType varchar(255), 
insertDate date, 
status varchar(255) default 'A', 
insertBy_uuid varchar(32) default 'e67f2870c59c11e39c1a0800200c9a66', 
collectedBy_uuid varchar(32) default '4786a2b0c59d11e39c1a0800200c9a66',
District varchar(255), 
SprayArea varchar(255), 
MapArea varchar(255), 
Sector varchar(255), 
Province varchar(255),
Site varchar(255),
UNIQUEBID varchar(255),
Latitude float,
Longitude float,
BuildingType int
);

insert into location_temp (Province, District, MapArea, Sector, Site, SprayArea, UNIQUEBID, Latitude, Longitude, BuildingType)
SELECT DISTINCT Province, District, MapArea, Sector, Site, SprayArea, UNIQUEBID, Latitude, Longitude, BuildingType FROM sample.sample_data
Where Province != 'UnknownProvince'
And District != 'UnknownDistrict'
And MapArea != 'UnknownMapArea'
And Sector IS NOT NULL;
UPDATE location_temp SET uuid = replace(uuid(), '-', '');
UPDATE location_temp SET insertDate = date(now());
UPDATE location_temp SET locationName = concat('LOC', UNIQUEBID);
UPDATE location_temp SET extId = concat('LOC', UNIQUEBID);
UPDATE location_temp SET locationType = 'Household Location';
UPDATE location_temp, sector_temp SET location_temp.locationLevel_uuid = sector_temp.uuid 
WHERE sector_temp.Province = location_temp.Province
AND sector_temp.District = location_temp.District
AND sector_temp.MapArea = location_temp.MapArea
AND sector_temp.Sector = location_temp.Sector;

INSERT INTO openhds.location (uuid,extId,locationName,locationLevel_uuid,locationType,insertDate,collectedBy_uuid,insertBy_uuid,status)
SELECT uuid,extId,locationName,locationLevel_uuid,locationType,insertDate,collectedBy_uuid,insertBy_uuid,status FROM location_temp;

-- household_temp: distinct UNIQUEBID, HeadOfHousehold gives social groups
-- sample data doesn't have enough information to create useful individuals
-- groupHead_uuid = 'Unknown Individual'
drop table if exists sample.household_temp;
create table sample.household_temp (
uuid varchar(32), 
extId varchar(255), 
groupName varchar(255),
groupType varchar(255),
groupHead_uuid varchar(32) default 'Unknown Individual',
insertDate date, 
status varchar(255) default 'A', 
insertBy_uuid varchar(32) default 'e67f2870c59c11e39c1a0800200c9a66', 
collectedBy_uuid varchar(32) default '4786a2b0c59d11e39c1a0800200c9a66',
UNIQUEBID varchar(255),
HeadOfHousehold varchar(255)
);

insert into household_temp (UNIQUEBID, HeadOfHousehold)
SELECT UNIQUEBID, HeadOfHousehold FROM sample.sample_data 
WHERE HeadOfHousehold != 'UnknownHeadOfHousehold';
UPDATE household_temp SET uuid = replace(uuid(), '-', '');
UPDATE household_temp SET insertDate = date(now());
UPDATE household_temp SET groupName = concat('Household of ', HeadOfHousehold);
UPDATE household_temp SET extId = UNIQUEBID;
UPDATE household_temp SET groupType = 'Household';

INSERT INTO openhds.socialgroup (uuid,extId,groupName,groupType,groupHead_uuid,insertDate,collectedBy_uuid,insertBy_uuid,status)
SELECT uuid,extId,groupName,groupType,groupHead_uuid,insertDate,collectedBy_uuid,insertBy_uuid,status FROM household_temp;

