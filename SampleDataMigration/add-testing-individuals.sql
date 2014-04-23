-- a temporary individual for testing purposes
drop table if exists sample.individual_temp;
create table sample.individual_temp (
uuid varchar(32), 
extId varchar(255), 
firstName varchar(255), 
lastName varchar(255), 
middleName varchar(255), 
gender varchar(32),
dob date,
mother_uuid varchar(32),
father_uuid varchar(32),
insertDate date, 
status varchar(255) default 'A', 
insertBy_uuid varchar(32) default 'e67f2870c59c11e39c1a0800200c9a66', 
collectedBy_uuid varchar(32) default '4786a2b0c59d11e39c1a0800200c9a66',
UNIQUEBID varchar(255)
);

insert into individual_temp (UNIQUEBID)
SELECT extId FROM openhds.socialgroup;
UPDATE individual_temp SET uuid = replace(uuid(), '-', '');
UPDATE individual_temp SET extId = concat('IND', UNIQUEBID);
UPDATE individual_temp SET firstName = 'First';
UPDATE individual_temp SET middleName = 'Middle Middle2 Middle3';
UPDATE individual_temp SET lastName = 'Last';
UPDATE individual_temp SET gender = 'F';
UPDATE individual_temp SET dob = date(now());
UPDATE individual_temp SET mother_uuid = 'Unknown Individual';
UPDATE individual_temp SET father_uuid = 'Unknown Individual';
UPDATE individual_temp SET insertDate = date(now());

INSERT INTO openhds.individual (uuid,extId,firstName,middleName,lastName,gender,dob,mother_uuid,father_uuid,insertBy_uuid,insertDate,status,collectedBy_uuid)
SELECT uuid,extId,firstName,middleName,lastName,gender,dob,mother_uuid,father_uuid,insertBy_uuid,insertDate,status,collectedBy_uuid FROM individual_temp;

 -- residency...
 -- membership...