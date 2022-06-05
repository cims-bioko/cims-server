-- add privilege for reprocessing campaign form submissions by binding
insert into privilege (uuid, privilege) values
(uuid_nodash(), 'EDIT_SUBMISSIONS');

-- grant to administrator role
insert into role_privileges (role, privilege)
select
  'ROLE1', uuid
from
  privilege
where
  privilege in ('EDIT_SUBMISSIONS');