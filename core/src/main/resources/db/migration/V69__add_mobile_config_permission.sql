-- add privs for device functionality
insert into privilege (uuid, privilege) values
(uuid_nodash(), 'VIEW_MOBILE_CONFIG_CODES');

-- grant to administrator role
insert into role_privileges (role, privilege)
select
  'ROLE1', uuid
from
  privilege
where
  privilege in ('VIEW_MOBILE_CONFIG_CODES');