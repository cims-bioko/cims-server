alter table locationhierarchy add column attrs jsonb;

update
  location l
set
  communityname = replace(communityname, concat(' ',communitycode),'')
from
  locationhierarchy s
join
  locationhierarchy m on s.parent_uuid = m.uuid
join
  locationhierarchy lc on m.parent_uuid = lc.uuid
where
  l.locationhierarchy_uuid = s.uuid
  and lc.name = l.communityname
  and l.communityname like concat('%',' ',l.communitycode);

update
  location l
set
  communityname = communitycode
from
  locationhierarchy s
join
  locationhierarchy m on s.parent_uuid = m.uuid
join
  locationhierarchy lc on m.parent_uuid = lc.uuid
where
  l.locationhierarchy_uuid = s.uuid and lc.name = 'Tope';

update
  locationhierarchy lc
set
  attrs = concat('{"name":"',l.communityname,'","code":"',l.communitycode,'"}')::jsonb
from
  location l, locationhierarchy m, locationhierarchy s
where
  s.parent_uuid = m.uuid
  and l.locationhierarchy_uuid = s.uuid
  and m.parent_uuid = lc.uuid
  and lc.name != 'Sin Localidad'
  and (communityname != '' or communitycode != '');

update locationhierarchy set name = attrs->>'name' where name = 'Tope';

create or replace view
  v_location_sync
as
select
  l.uuid,
  l.extId,
  locationHierarchy_uuid as hierarchyUuid,
  ls.extId as hierarchyExtId,
  case locationName when 'null' then 'Unnamed' else locationName end as name,
  description,
  (ll.attrs->>'name')::varchar(255) as communityName,
  (ll.attrs->>'code')::varchar(255) as communityCode,
  ll.name as localityName,
  lm.name as mapAreaName,
  ls.name as sectorName,
  buildingNumber,
  floorNumber,
  st_y(global_pos)::varchar as latitude,
  st_x(global_pos)::varchar as longitude
from
  location l
join
  locationhierarchy ls on l.locationHierarchy_uuid = ls.uuid
join
  locationhierarchy lm on ls.parent_uuid = lm.uuid
join
  locationhierarchy ll on lm.parent_uuid = ll.uuid
where
  not l.deleted;

alter table location drop column regionname, drop column provincename, drop column districtname,
  drop column subdistrictname, drop column communityname, drop column communitycode,drop column localityname,
  drop column mapareaname, drop column sectorname;

drop trigger update_admin_columns on location;
drop function update_location_admin_columns_from_hierarchy();