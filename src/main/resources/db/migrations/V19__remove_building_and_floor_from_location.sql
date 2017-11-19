create type uniquebid as (
  formatted text,
  map_name text,
  map integer,
  sector_name text,
  sector integer,
  building_name text,
  building integer,
  floor_name text,
  floor integer,
  duplicate_name text,
  duplicate integer
);

create or replace function to_uniquebid(str text) returns uniquebid as $$
declare
  parsed text[];
  result uniquebid;
begin
  select regexp_matches(str, '((M(\d{4}))(S(\d{3}))(E(\d{3}))(P(\d{2}))(-d(\d+))?)') into parsed;
  if FOUND then
    result.formatted := parsed[1];
    result.map_name := parsed[2];
    result.map := parsed[3]::integer;
    result.sector_name := parsed[4];
    result.sector := parsed[5]::integer;
    result.building_name := parsed[6];
    result.building := parsed[7]::integer;
    result.floor_name := parsed[8];
    result.floor := parsed[9]::integer;
    result.duplicate_name := parsed[10];
    result.duplicate := parsed[11]::integer;
    return result;
  else
    return null;
  end if;
end$$ language plpgsql;

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
  (to_uniquebid(l.extid)).building as buildingNumber,
  (to_uniquebid(l.extid)).floor as floorNumber,
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

alter table location drop column buildingnumber, drop column floornumber;
