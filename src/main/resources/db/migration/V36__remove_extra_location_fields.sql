drop view v_location_sync;
create view v_location_sync as
select
  l.uuid,
  l.extid,
  l.hierarchy as hierarchyuuid,
  coalesce(l.name,'') as name,
  description,
  lm.name as mapareaname,
  ls.name as sectorname,
  (to_uniquebid(l.extid)).building as buildingnumber,
  st_y(global_pos)::varchar as latitude,
  st_x(global_pos)::varchar as longitude,
  l.attrs::text as attrs
from
  location l
join
  locationhierarchy ls on l.hierarchy = ls.uuid
join
  locationhierarchy lm on ls.parent = lm.uuid
join
  locationhierarchy ll on lm.parent = ll.uuid
where
  not l.deleted;