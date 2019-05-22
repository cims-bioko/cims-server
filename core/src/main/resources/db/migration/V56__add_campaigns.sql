-- add table for campaigns
create table campaign (
  uuid varchar(32) primary key,
  name varchar(255) not null,
  description varchar(255),
  version varchar(255),
  created timestamp not null default current_timestamp,
  deleted timestamp,
  start timestamp,
  "end" timestamp
);

create table device (
  uuid varchar(32) primary key,
  name varchar(255) not null,
  description varchar(255) not null,
  created timestamp not null default current_timestamp,
  deleted timestamp,
  token varchar(255) not null
);

create table campaign_devices (
    campaign varchar(32) not null,
    device varchar(32) not null
);

alter table campaign_devices add foreign key (campaign) references campaign(uuid) on delete cascade;
alter table campaign_devices add foreign key (device) references device(uuid) on delete cascade;

create table campaign_members (
    campaign varchar(32) not null,
    "user" varchar(32) not null
);

alter table campaign_members add foreign key (campaign) references campaign(uuid) on delete cascade;
alter table campaign_members add foreign key ("user") references users(uuid) on delete cascade;

create table campaign_forms (
    campaign varchar(32) not null,
    form_id varchar(255) not null,
    form_version varchar(255) not null
);

alter table campaign_forms add foreign key (campaign) references campaign(uuid) on delete cascade;
alter table campaign_forms add foreign key (form_id, form_version) references form(id, version) on delete cascade;