create table form_submission (instanceId varchar(255), as_xml xml not null, as_json jsonb not null, form_id varchar(255) not null, form_version varchar(255) not null, form_binding varchar(255) not null, from_device varchar(255) not null, collected timestamp, submitted timestamp not null default current_timestamp, processed timestamp, processed_ok boolean, primary key (instanceId));
create index submission_device_idx on form_submission(from_device);
create index submitted_idx on form_submission(submitted desc);
create index form_id_idx on form_submission(form_id);
create index form_version_idx on form_submission(form_version);
create index as_json_idx on form_submission using gin(as_json);
create index collected_idx on form_submission(collected);
create index processed_idx on form_submission(processed);
create index processed_ok_idx on form_submission(processed_ok);

-- Introduce a utility user for system tasks, like form processing.
insert into users (uuid, firstName, lastName, fullName, description, username, password, lastLoginTime, deleted) values ('system-uuid', 'System', 'User', 'System User', 'Internal user for system-directed tasks', 'system', md5(random()::text), 0, false);
insert into user_roles (user_uuid, role_uuid) values ('system-uuid', 'ROLE1');