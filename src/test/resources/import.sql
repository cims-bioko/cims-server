-- Defined core privileges
INSERT INTO privilege VALUES ('PRIVILEGE1', 'CREATE_ENTITY')
INSERT INTO privilege VALUES ('PRIVILEGE2', 'EDIT_ENTITY')
INSERT INTO privilege VALUES ('PRIVILEGE3', 'DELETE_ENTITY')
INSERT INTO privilege VALUES ('PRIVILEGE4', 'VIEW_ENTITY')
INSERT INTO privilege VALUES ('PRIVILEGE5', 'CREATE_USER')
INSERT INTO privilege VALUES ('PRIVILEGE6', 'DELETE_USER')
INSERT INTO privilege VALUES ('PRIVILEGE7', 'ACCESS_BASELINE')
INSERT INTO privilege VALUES ('PRIVILEGE8', 'ACCESS_UPDATE')
INSERT INTO privilege VALUES ('PRIVILEGE9', 'ACCESS_AMENDMENT_FORMS')
INSERT INTO privilege VALUES ('PRIVILEGE11', 'ACESSS_UTILITY_ROUTINES')

-- Defined  core roles
INSERT INTO role (uuid, name, description, deleted) VALUES ('ROLE1', 'ADMINISTRATOR', 'Administrator of OpenHDS', false)
INSERT INTO role (uuid, name, description, deleted) VALUES ('ROLE2', 'DATA CLERK', 'Data Clerk of OpenHDS', false)
INSERT INTO role (uuid, name, description, deleted) VALUES ('ROLE3', 'DATA MANAGER', 'Data Manager of OpenHDS', false)
INSERT INTO role (uuid, name, description, deleted) VALUES ('ROLE4', 'TEST USER', 'Test User of OpenHDS', false)
INSERT INTO role_privileges (role, privilege) VALUES ('ROLE1', 'PRIVILEGE1')
INSERT INTO role_privileges (role, privilege) VALUES ('ROLE1', 'PRIVILEGE2')
INSERT INTO role_privileges (role, privilege) VALUES ('ROLE1', 'PRIVILEGE3')
INSERT INTO role_privileges (role, privilege) VALUES ('ROLE1', 'PRIVILEGE4')
INSERT INTO role_privileges (role, privilege) VALUES ('ROLE1', 'PRIVILEGE5')
INSERT INTO role_privileges (role, privilege) VALUES ('ROLE1', 'PRIVILEGE6')
INSERT INTO role_privileges (role, privilege) VALUES ('ROLE1', 'PRIVILEGE7')
INSERT INTO role_privileges (role, privilege) VALUES ('ROLE1', 'PRIVILEGE8')
INSERT INTO role_privileges (role, privilege) VALUES ('ROLE1', 'PRIVILEGE9')
INSERT INTO role_privileges (role, privilege) VALUES ('ROLE1', 'PRIVILEGE10')
INSERT INTO role_privileges (role, privilege) VALUES ('ROLE1', 'PRIVILEGE11')

INSERT INTO role_privileges (role, privilege) VALUES ('ROLE2', 'PRIVILEGE4')
INSERT INTO role_privileges (role, privilege) VALUES ('ROLE4', 'PRIVILEGE1')
INSERT INTO role_privileges (role, privilege) VALUES ('ROLE4', 'PRIVILEGE8')

-- Defined Admin user
INSERT INTO users (uuid, first_Name, last_Name, full_name, description, username, password, last_login, deleted) VALUES ('User 1', 'FirstName', 'LastName', 'Administrator', 'Administrator User', 'admin', 'test', 0, false)
INSERT INTO user_roles (user, role) VALUES ('User 1', 'ROLE1')
INSERT INTO users (uuid, first_Name, last_Name, full_name, description, username, password, last_login, deleted) VALUES ('User 2', 'Test', 'Account', 'Test Account', 'Test User Account', 'test', 'test', 0, false)
INSERT INTO user_roles (user, role) VALUES ('User 2', 'ROLE4')
INSERT INTO users (uuid, first_Name, last_Name, full_name, description, username, password, last_login, deleted) VALUES ('User 3', 'DataClerk', 'Account', 'Test Account', 'Test User Account', 'dataclerk', 'dataclerk', 0, false)
INSERT INTO user_roles (user, role) VALUES ('User 3', 'ROLE2')

-- Location Hierarchy root
INSERT INTO locationhierarchy(uuid,name,extId,level,parent) VALUES('hierarchy_root','', 'HIERARCHY_ROOT', NULL,NULL)

-- Field Worker
INSERT INTO fieldworker (uuid, extid, first_name, last_name, password_hash, deleted, idPrefix) VALUES ('UnknownFieldWorker','UNK', 'Unknown', 'FieldWorker', 'invalid-password-hash', false, 56);
INSERT INTO fieldworker (uuid, extid, first_name, last_name, password_hash, deleted, idPrefix) VALUES ('FieldWorker1','FWEK1D', 'Editha', 'Kaweza', 'invalid-password-hash', false, 65)

-- Unknown Individual: This should always be pre-populated
INSERT INTO individual(uuid,extId,first_Name,middle_Name,last_Name,gender,dob,created,deleted,collector,phone1,phone2,language,contactName,contactPhone,dip) VALUES('Unknown Individual','UNK','Unknown',NULL,'UNKNOWN','M', '1900-12-19','2009-12-19',false,'UnknownFieldWorker','123-456-7890','123-456-7890','Spanish','Bob Johnson','123-456-7890','23')
INSERT INTO individual(uuid,extId,first_Name,middle_Name,last_Name,gender,dob,created,deleted,collector,phone1,phone2,language,contactName,contactPhone,dip) VALUES ('Individual2','NBAS1I','Nancy',NULL,'Bassey','F', '1959-12-19','2009-12-19',false,'FieldWorker1','123-456-7890','123-456-7890','Spanish','Bob Johnson','123-456-7890','23')
INSERT INTO individual(uuid,extId,first_Name,middle_Name,last_Name,gender,dob,created,deleted,collector,phone1,phone2,language,contactName,contactPhone,dip) VALUES ('Individual3','BJOH1J','Bob',NULL,'Johnson','M', '1965-12-19','2009-12-19',false,'FieldWorker1','123-456-7890','123-456-7890','Spanish','Bob Johnson','123-456-7890','23')
INSERT INTO individual(uuid,extId,first_Name,middle_Name,last_Name,gender,dob,created,deleted,collector,phone1,phone2,language,contactName,contactPhone,dip) VALUES ('Individual4','CBLA1H','Cristen',NULL,'Blake','F', '1960-12-19','2009-12-19',false,'FieldWorker1','123-456-7890','123-456-7890','Spanish','Bob Johnson','123-456-7890','23')
INSERT INTO individual(uuid,extId,first_Name,middle_Name,last_Name,gender,dob,created,deleted,collector,phone1,phone2,language,contactName,contactPhone,dip) VALUES ('Individual5','BHAR1K','Brian',NULL,'Blake','M', '1965-12-19','2009-12-19',false,'FieldWorker1','123-456-7890','123-456-7890','Spanish','Bob Johnson','123-456-7890','23')

-- Location Hierarchy Levels, these must be configured
INSERT INTO locationhierarchylevel(uuid,keyId,name) VALUES('HierarchyLevel1',1,'LGA')
INSERT INTO locationhierarchylevel(uuid,keyId,name) VALUES('HierarchyLevel2',2,'Ward')
INSERT INTO locationhierarchylevel(uuid,keyId,name) VALUES('HierarchyLevel3',3,'Village')

INSERT INTO location(uuid,extId,name,hierarchy,type,created,deleted,collector) VALUES ('LOCATION1','NJA001','House 3','hierarchy_root','RUR','2000-12-19',false,'FieldWorker1')

INSERT INTO socialgroup(uuid,extId,deleted,created,name,collector,head,type) VALUES ('BasseyFamily', 'MBI1', false, '1979-12-19', 'Bassey Family', 'FieldWorker1','Individual2','FAM')