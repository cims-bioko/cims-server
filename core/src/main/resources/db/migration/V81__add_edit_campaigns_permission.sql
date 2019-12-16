-- add privs for campaign upload/download
insert into privilege (uuid, privilege) values
(uuid_nodash(), 'EDIT_CAMPAIGNS');

-- grant to administrator role
insert into role_privileges (role, privilege)
select
  'ROLE1', uuid
from
  privilege
where
  privilege in ('EDIT_CAMPAIGNS');