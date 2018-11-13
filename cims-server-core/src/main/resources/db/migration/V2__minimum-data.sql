-- required data set import script for version 0.9.0

-- Defined core privileges
insert into privilege values ('PRIVILEGE1', 'CREATE_ENTITY');
insert into privilege values ('PRIVILEGE2', 'EDIT_ENTITY');
insert into privilege values ('PRIVILEGE3', 'DELETE_ENTITY');
insert into privilege values ('PRIVILEGE4', 'VIEW_ENTITY');
insert into privilege values ('PRIVILEGE5', 'CREATE_USER');
insert into privilege values ('PRIVILEGE6', 'DELETE_USER');
insert into privilege values ('PRIVILEGE7', 'ACCESS_BASELINE');
insert into privilege values ('PRIVILEGE8', 'ACCESS_UPDATE');
insert into privilege values ('PRIVILEGE9', 'ACCESS_AMENDMENT_FORMS');
insert into privilege values ('PRIVILEGE10', 'ACCESS_REPORTS');
insert into privilege values ('PRIVILEGE11', 'ACESSS_UTILITY_ROUTINES');
insert into privilege values ('PRIVILEGE12', 'ACESSS_CONFIGURATION');

-- Defined  core roles
insert into role (uuid, name, description, deleted) values ('ROLE1', 'ADMINISTRATOR', 'Administrator of OpenHDS', false);
insert into role (uuid, name, description, deleted) values ('ROLE2', 'DATA CLERK', 'Data Clerk of OpenHDS', false);
insert into role (uuid, name, description, deleted) values ('ROLE3', 'DATA MANAGER', 'Data Manager of OpenHDS', false);
insert into role_privileges (role_uuid, privilege_uuid) values ('ROLE1', 'PRIVILEGE1');
insert into role_privileges (role_uuid, privilege_uuid) values ('ROLE1', 'PRIVILEGE2');
insert into role_privileges (role_uuid, privilege_uuid) values ('ROLE1', 'PRIVILEGE3');
insert into role_privileges (role_uuid, privilege_uuid) values ('ROLE1', 'PRIVILEGE4');
insert into role_privileges (role_uuid, privilege_uuid) values ('ROLE1', 'PRIVILEGE5');
insert into role_privileges (role_uuid, privilege_uuid) values ('ROLE1', 'PRIVILEGE6');
insert into role_privileges (role_uuid, privilege_uuid) values ('ROLE1', 'PRIVILEGE7');
insert into role_privileges (role_uuid, privilege_uuid) values ('ROLE1', 'PRIVILEGE8');
insert into role_privileges (role_uuid, privilege_uuid) values ('ROLE1', 'PRIVILEGE9');
insert into role_privileges (role_uuid, privilege_uuid) values ('ROLE1', 'PRIVILEGE10');
insert into role_privileges (role_uuid, privilege_uuid) values ('ROLE1', 'PRIVILEGE11');
insert into role_privileges (role_uuid, privilege_uuid) values ('ROLE1', 'PRIVILEGE12');

insert into role_privileges (role_uuid, privilege_uuid) values ('ROLE2', 'PRIVILEGE4');

-- Defined Admin user
insert into users (uuid, firstName, lastName, fullName, description, username, password, lastLoginTime, deleted) values ('User 1', 'FirstName', 'LastName', 'Administrator', 'Administrator User', 'admin', 'test', 0, false);
insert into user_roles (user_uuid, role_uuid) values ('User 1', 'ROLE1');

-- Location Hierarchy root
insert into locationhierarchy(uuid,name,extId,level_uuid,parent_uuid) values('hierarchy_root','', 'HIERARCHY_ROOT', null,null);

-- Field Worker
insert into fieldworker (uuid, extid, firstname, lastname, passwordHash, deleted, idPrefix) values ('UnknownFieldWorker','UNK', 'Unknown', 'FieldWorker', 'invalid-password-hash', false, 86);

-- Unknown Individual: This should always be pre-populated
insert into individual(uuid,extId,firstName,middleName,lastName,gender,dob,mother_uuid,father_uuid,insertBy_uuid,insertDate,status,voidDate,voidReason,voidBy_uuid,deleted,collectedBy_uuid,age,ageUnits,phoneNumber,otherPhoneNumber,languagePreference,pointOfContactName,pointOfContactPhoneNumber,dip,memberStatus) values('Unknown Individual','UNK','Unknown',null,'UNKNOWN','MALE', '1900-12-19 15:07:43', null, null,'User 1','2009-12-19 15:07:43','PENDING',null,null,null,false,'UnknownFieldWorker',0,null,null,null,null,null,null,0,null);

insert into whitelist (uuid, address) values ('LOCALHOST1', '127.0.0.1');
insert into whitelist (uuid, address) values ('LOCALHOST2', 'localhost');