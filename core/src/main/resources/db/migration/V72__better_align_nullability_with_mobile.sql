alter table location
  alter name set default '',
  alter name set not null;

alter table fieldworker
  alter first_name set default '',
  alter first_name set not null,
  alter last_name set default '',
  alter last_name set not null;

alter table individual
  alter first_name set default '',
  alter first_name set not null,
  alter last_name set default '',
  alter last_name set not null;

drop view v_location_sync;

create view v_location_sync as
select
  l.uuid,
  l.extid,
  hierarchy as hierarchyuuid,
  l.name AS name,
  description,
  lm.name AS mapareaname,
  ls.name AS sectorname,
  (to_uniquebid(l.extid)).building AS buildingnumber,
  st_y(global_pos)::character varying AS latitude,
  st_x(global_pos)::character varying AS longitude,
  l.attrs::text AS attrs
from
  location l
join
  locationhierarchy ls on l.hierarchy = ls.uuid
join
  locationhierarchy lm ON ls.parent = lm.uuid
join
  locationhierarchy ll ON lm.parent = ll.uuid
where
  deleted is null;