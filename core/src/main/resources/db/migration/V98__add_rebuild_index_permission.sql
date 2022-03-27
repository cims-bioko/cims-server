-- add privilege for rebuilding search indices
insert into privilege (uuid, privilege) values
(uuid_nodash(), 'REBUILD_INDEX');

-- grant to administrator role
insert into role_privileges (role, privilege)
select
  'ROLE1', uuid
from
  privilege
where
  privilege in ('REBUILD_INDEX');

