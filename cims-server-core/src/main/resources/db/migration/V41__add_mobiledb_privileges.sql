/* add permissions so we can manage mobile sync and export access */
insert into privilege (uuid, privilege) values ('PRIVILEGE13', 'MOBILE_DB_SYNC');
insert into privilege (uuid, privilege) values ('PRIVILEGE14', 'MOBILE_DB_EXPORT');

/* add these to the admin role */
insert into role_privileges (role, privilege) values ('ROLE1', 'PRIVILEGE13');
insert into role_privileges (role, privilege) values ('ROLE1', 'PRIVILEGE14');

/* add these to the supervisor role */
insert into role_privileges (role, privilege) values ('ROLE3', 'PRIVILEGE13');
insert into role_privileges (role, privilege) values ('ROLE3', 'PRIVILEGE14');
