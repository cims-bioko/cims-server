-- add table supplying device to role mappings
create table device_roles (
    device varchar(32) not null,
    role varchar(32) not null,
    primary key(device, role)
);

alter table device_roles add foreign key (device) references device(uuid) on delete cascade;
alter table device_roles add foreign key (role) references role(uuid) on delete cascade;

-- add the default role for devices
insert into role (uuid, created, name) values ('ROLE4', now(), 'DEVICE');

-- assign default permissions for devices
insert into role_privileges (role, privilege)
select 'ROLE4', uuid from privilege where privilege in (
 'MOBILE_SYNC', 'ODK_FORM_LIST', 'ODK_FORM_DOWNLOAD', 'ODK_SUBMISSION_UPLOAD'
);
