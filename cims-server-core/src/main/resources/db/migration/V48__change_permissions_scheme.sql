-- wipe existing privileges from roles
delete from role_privileges;

-- wipe existing privileges
delete from privilege;

-- insert new privileges
insert into privilege (uuid, privilege) values
(uuid_nodash(), 'MOBILE_SYNC'),
(uuid_nodash(), 'ODK_FORM_UPLOAD'),
(uuid_nodash(), 'ODK_FORM_DOWNLOAD'),
(uuid_nodash(), 'ODK_FORM_LIST'),
(uuid_nodash(), 'ODK_SUBMISSION_UPLOAD'),
(uuid_nodash(), 'ODK_SUBMISSION_LIST'),
(uuid_nodash(), 'ODK_SUBMISSION_DOWNLOAD'),
(uuid_nodash(), 'VIEW_FORMS'),
(uuid_nodash(), 'FORM_UPLOAD'),
(uuid_nodash(), 'FORM_UPLOAD_XLS'),
(uuid_nodash(), 'MANAGE_FORMS'),
(uuid_nodash(), 'VIEW_SYNC'),
(uuid_nodash(), 'MANAGE_SYNC'),
(uuid_nodash(), 'EXPORT_SYNC'),
(uuid_nodash(), 'VIEW_BACKUPS'),
(uuid_nodash(), 'CREATE_BACKUPS'),
(uuid_nodash(), 'MANAGE_BACKUPS'),
(uuid_nodash(), 'VIEW_USERS'),
(uuid_nodash(), 'CREATE_USERS'),
(uuid_nodash(), 'EDIT_USERS'),
(uuid_nodash(), 'DELETE_USERS'),
(uuid_nodash(), 'VIEW_ROLES'),
(uuid_nodash(), 'CREATE_ROLES'),
(uuid_nodash(), 'EDIT_ROLES'),
(uuid_nodash(), 'DELETE_ROLES'),
(uuid_nodash(), 'VIEW_FIELDWORKERS'),
(uuid_nodash(), 'CREATE_FIELDWORKERS'),
(uuid_nodash(), 'EDIT_FIELDWORKERS'),
(uuid_nodash(), 'DELETE_FIELDWORKERS');

-- setup administrator role (all privileges)
insert into role_privileges (role, privilege)
select 'ROLE1', uuid from privilege;

-- setup data clerk role
insert into role_privileges (role, privilege)
select 'ROLE2', uuid from privilege where privilege in (
  'MOBILE_SYNC',
  'ODK_FORM_LIST',
  'ODK_FORM_DOWNLOAD',
  'ODK_SUBMISSION_UPLOAD'
);

-- setup data manager role
insert into role_privileges (role, privilege)
select 'ROLE3', uuid from privilege where privilege in (
  'MOBILE_SYNC',
  'ODK_FORM_LIST',
  'ODK_FORM_DOWNLOAD',
  'ODK_SUBMISSION_LIST',
  'ODK_SUBMISSION_DOWNLOAD',
  'VIEW_FORMS',
  'VIEW_SYNC',
  'EXPORT_SYNC'
);

-- remove data exporter role
delete from user_roles where role = 'ROLE4';
delete from role where uuid = 'ROLE4';