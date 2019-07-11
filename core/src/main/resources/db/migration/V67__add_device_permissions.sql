-- add privs for device functionality
insert into privilege (uuid, privilege) values
(uuid_nodash(), 'CREATE_DEVICES'),
(uuid_nodash(), 'EDIT_DEVICES'),
(uuid_nodash(), 'DELETE_DEVICES'),
(uuid_nodash(), 'RESTORE_DEVICES'),
(uuid_nodash(), 'VIEW_DEVICES');

-- grant to administrator role
insert into role_privileges (role, privilege)
select
  'ROLE1', uuid
from
  privilege
where
  privilege in ('CREATE_DEVICES', 'EDIT_DEVICES', 'DELETE_DEVICES', 'RESTORE_DEVICES', 'VIEW_DEVICES');