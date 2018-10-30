insert into privilege (uuid, privilege) values
(uuid_nodash(), 'CREATE_HIERARCHY');

-- grant to administrator role
insert into role_privileges (role, privilege)
select 'ROLE1', uuid from privilege where privilege = 'CREATE_HIERARCHY';
