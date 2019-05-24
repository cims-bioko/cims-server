-- add column to track which user created the device
alter table device add column creator varchar(32);
update device set creator = (select uuid from users where username = 'admin') where creator is null;
alter table device alter column creator set not null;
alter table device add foreign key (creator) references users(uuid);
