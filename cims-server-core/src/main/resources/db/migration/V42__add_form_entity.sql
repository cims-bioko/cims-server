create table form (
  id varchar(255),
  version varchar(255),
  as_xml xml not null,
  downloads boolean not null default true,
  submissions boolean not null default true,
  uploaded timestamp not null default current_timestamp,
  last_submission timestamp,
  primary key(id, version)
);