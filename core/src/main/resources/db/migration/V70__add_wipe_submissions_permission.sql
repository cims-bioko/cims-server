-- add privs for wiping form submissions
insert into privilege (uuid, privilege) values
(uuid_nodash(), 'WIPE_FORM_SUBMISSIONS');

-- grant to administrator role
insert into role_privileges (role, privilege)
select
  'ROLE1', uuid
from
  privilege
where
  privilege in ('WIPE_FORM_SUBMISSIONS');