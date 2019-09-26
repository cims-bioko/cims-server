-- add privs for campaign upload/download
insert into privilege (uuid, privilege) values
(uuid_nodash(), 'UPLOAD_CAMPAIGNS'),
(uuid_nodash(), 'DOWNLOAD_CAMPAIGNS'),
(uuid_nodash(), 'VIEW_CAMPAIGNS');

-- grant to administrator role
insert into role_privileges (role, privilege)
select
  'ROLE1', uuid
from
  privilege
where
  privilege in ('UPLOAD_CAMPAIGNS', 'DOWNLOAD_CAMPAIGNS', 'VIEW_CAMPAIGNS');

-- grant to device role
insert into role_privileges (role, privilege)
select
  'ROLE4', uuid
from
  privilege
where
  privilege in ('UPLOAD_CAMPAIGNS', 'DOWNLOAD_CAMPAIGNS', 'VIEW_CAMPAIGNS');