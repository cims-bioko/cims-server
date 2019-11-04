-- add priv for creating a campaign
insert into privilege (uuid, privilege) values
(uuid_nodash(), 'CREATE_CAMPAIGNS');

-- grant to administrator role
insert into role_privileges (role, privilege)
select
  'ROLE1', uuid
from
  privilege
where
  privilege in ('CREATE_CAMPAIGNS');