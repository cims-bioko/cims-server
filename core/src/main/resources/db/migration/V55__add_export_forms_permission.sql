insert into privilege (uuid, privilege) values
(uuid_nodash(), 'EXPORT_FORMS');

-- grant to administrator role
insert into role_privileges (role, privilege)
select 'ROLE1', uuid from privilege where privilege = 'EXPORT_FORMS';

-- grant to data manager
insert into role_privileges (role, privilege)
select 'ROLE3', uuid from privilege where privilege = 'EXPORT_FORMS';