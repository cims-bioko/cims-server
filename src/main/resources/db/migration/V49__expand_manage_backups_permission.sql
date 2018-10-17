-- remove unused manage backups privilege
delete from privilege where privilege = 'MANAGE_BACKUPS';

-- add edit/delete backup privs
insert into privilege (uuid, privilege) values
(uuid_nodash(), 'EDIT_BACKUPS'),
(uuid_nodash(), 'DELETE_BACKUPS');

-- grant them to admin role
insert into role_privileges (role, privilege)
select 'ROLE1', uuid from privilege where privilege in ('EDIT_BACKUPS', 'DELETE_BACKUPS');