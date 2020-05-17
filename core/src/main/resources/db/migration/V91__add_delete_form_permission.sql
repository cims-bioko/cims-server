-- add privs for permanently deleting forms
insert into privilege (uuid, privilege) values
(uuid_nodash(), 'DELETE_FORMS');

-- grant to administrator role
insert into role_privileges (role, privilege)
select
  'ROLE1', uuid
from
  privilege
where
  privilege in ('DELETE_FORMS');