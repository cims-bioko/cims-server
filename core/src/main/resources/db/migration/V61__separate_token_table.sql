alter table device rename column token_hash to token;

-- migrate tokens to their own table to enable users to use as well
create table access_token (
  value varchar(255) primary key,
  created timestamp not null default current_timestamp,
  expires timestamp not null default current_timestamp + interval '1 week'
);

-- migrate existing tokens to new scheme
insert into access_token
select token from device;

-- ensure device hash values are associated with a token record
alter table device add foreign key (token) references access_token(value);

-- ensure only one device is associated with a given hash value
create unique index on device(token);