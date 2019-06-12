alter table device add column secret varchar(255) not null default 'disabled';
alter table device add column secret_expires varchar(255) default current_timestamp + interval '1 hour';
