drop view v_location_sync;
create view v_location_sync as
select
  l.uuid,
  l.extid,
  hierarchy as hierarchyuuid,
  l.name AS name,
  description,
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