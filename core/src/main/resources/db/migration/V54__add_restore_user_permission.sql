insert into privilege (uuid, privilege) values
(uuid_nodash(), 'RESTORE_USERS');

-- grant to administrator role
insert into role_privileges (role, privilege)
select 'ROLE1', uuid from privilege where privilege = 'RESTORE_USERS';