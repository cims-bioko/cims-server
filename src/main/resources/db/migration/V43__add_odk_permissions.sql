-- add permissions used to secure odk api endpoints
insert into privilege (uuid, privilege) values ('PRIVILEGE15', 'FORM_UPLOAD');
insert into privilege (uuid, privilege) values ('PRIVILEGE16', 'FORM_LIST');
insert into privilege (uuid, privilege) values ('PRIVILEGE17', 'FORM_DOWNLOAD');
insert into privilege (uuid, privilege) values ('PRIVILEGE18', 'FORM_DELETE');
insert into privilege (uuid, privilege) values ('PRIVILEGE19', 'SUBMISSION_UPLOAD');
insert into privilege (uuid, privilege) values ('PRIVILEGE20', 'SUBMISSION_LIST');
insert into privilege (uuid, privilege) values ('PRIVILEGE21', 'SUBMISSION_DOWNLOAD');

-- grant administrator all of the new privileges
insert into role_privileges (role, privilege) values ('ROLE1', 'PRIVILEGE15');
insert into role_privileges (role, privilege) values ('ROLE1', 'PRIVILEGE16');
insert into role_privileges (role, privilege) values ('ROLE1', 'PRIVILEGE17');
insert into role_privileges (role, privilege) values ('ROLE1', 'PRIVILEGE18');
insert into role_privileges (role, privilege) values ('ROLE1', 'PRIVILEGE19');
insert into role_privileges (role, privilege) values ('ROLE1', 'PRIVILEGE20');
insert into role_privileges (role, privilege) values ('ROLE1', 'PRIVILEGE21');

-- grant supervisor enough to use Collect
insert into role_privileges (role, privilege) values ('ROLE3', 'PRIVILEGE16');
insert into role_privileges (role, privilege) values ('ROLE3', 'PRIVILEGE17');
insert into role_privileges (role, privilege) values ('ROLE3', 'PRIVILEGE19');
insert into role_privileges (role, privilege) values ('ROLE3', 'PRIVILEGE20');
insert into role_privileges (role, privilege) values ('ROLE3', 'PRIVILEGE21');

-- grant data clerk enough to use Collect
insert into role_privileges (role, privilege) values ('ROLE2', 'PRIVILEGE16');
insert into role_privileges (role, privilege) values ('ROLE2', 'PRIVILEGE17');
insert into role_privileges (role, privilege) values ('ROLE2', 'PRIVILEGE19');

-- create new role for briefcase export
insert into role (uuid, name, deleted) values ('ROLE4','DATA EXPORTER', false);

-- grant export user enough to use briefcase's pull functionality
insert into role_privileges (role, privilege) values ('ROLE4', 'PRIVILEGE16');
insert into role_privileges (role, privilege) values ('ROLE4', 'PRIVILEGE17');
insert into role_privileges (role, privilege) values ('ROLE4', 'PRIVILEGE20');
insert into role_privileges (role, privilege) values ('ROLE4', 'PRIVILEGE21');
