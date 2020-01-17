insert into campaign (uuid, name, description, "default")
values (uuid_nodash(), 'default', 'System-generated default campaign', true)
on conflict do nothing;