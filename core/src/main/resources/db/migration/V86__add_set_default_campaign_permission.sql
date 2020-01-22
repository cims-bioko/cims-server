-- add privs for campaign upload/download
insert into privilege (uuid, privilege) values
(uuid_nodash(), 'SET_DEFAULT_CAMPAIGN');

-- grant to administrator role
insert into role_privileges (role, privilege)
select
  'ROLE1', uuid
from
  privilege
where
  privilege in ('SET_DEFAULT_CAMPAIGN');