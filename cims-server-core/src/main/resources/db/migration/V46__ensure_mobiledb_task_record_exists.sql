create unique index on task(name);
insert into task (uuid, name, itemcount) values (uuid_nodash(), 'Mobile DB Task', 0) on conflict(name) do nothing;