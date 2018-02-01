/* role_privileges should be pruned when either side is deleted */
alter table role_privileges drop constraint fk797eef4b1920cbbe;
alter table role_privileges drop constraint fk797eef4b21e9ac56;
alter table role_privileges add foreign key (role) references role(uuid) on delete cascade;
alter table role_privileges add foreign key (privilege) references privilege(uuid) on delete cascade;

/* remove all privileges known to be unused after application changes */
delete from
  privilege
where
  privilege not in (
  'CREATE_ENTITY',
  'DELETE_ENTITY',
  'VIEW_ENTITY',
  'EDIT_ENTITY',
  'ACESSS_UTILITY_ROUTINES',
  'ACESSS_CONFIGURATION');

/* wipe the role descriptions, they are useless right now*/
update role set description = null where uuid in ('ROLE1', 'ROLE2', 'ROLE3');

/* correct spelling of access privileges */
update privilege set privilege = replace(privilege, 'ACESSS', 'ACCESS');