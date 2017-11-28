-- eliminate 'null' values left over from mirth-based setup's poor null handling
do $$
declare
  col record;
begin
  for col in select table_name, column_name from information_schema.columns
             where table_schema = current_schema() and data_type = 'character varying'
                   and table_name not like 'v_%' and is_nullable = 'YES'
  loop
    execute concat('update ', col.table_name, ' set ', col.column_name, ' = null where ', col.column_name, ' = ''null''');
  end loop;
end $$;

-- eliminate zeros from dip
update individual set dip = null where dip = 0;

-- redefine individual sync view - dropping due to data type changes
drop view v_individual_sync;
create or replace view
  v_individual_sync
as
with last_residence as (
  select distinct on (m.individual_uuid)
    m.individual_uuid, g.location_uuid
  from
    membership m
  join
    socialgroup g on m.socialgroup_uuid = g.uuid
  where
    not m.deleted
    and not g.deleted
  order by
    m.individual_uuid, m.insertdate desc)
select
  i.uuid,
  to_char(i.dob,'YYYY-MM-DD') as dob,
  i.extId,
  'Unknown Individual'::varchar as father,
  i.firstname,
  i.gender,
  i.lastname,
  'Unknown Individual'::varchar as mother,
  lr.location_uuid as currentresidence,
  'NA'::varchar(255) as endtype,
  i.middleName as othernames,
  (case when dob > now() - interval '1 year' then date_part('month',age(i.dob)) else date_part('year', age(i.dob)) end)::int as age,
  (case when i.dob > now() - interval '1 year' then 'Months' else 'Years' end) as ageunits,
  i.phonenumber,
  i.otherphonenumber,
  i.pointofcontactname,
  i.pointofcontactphonenumber,
  i.languagepreference,
  'Unknown'::varchar as memberstatus,
  i.nationality,
  i.dip as otherid
from
  individual i
left join
  last_residence lr on i.uuid = lr.individual_uuid
where
  not i.deleted
  and i.extId <> 'UNK'
  and lr.location_uuid is not null;

-- redefine location sync view - dropping due to data type changes
drop view v_location_sync;
create or replace view
  v_location_sync
as
select
  l.uuid,
  l.extid,
  locationhierarchy_uuid as hierarchyuuid,
  ls.extid as hierarchyextid,
  coalesce(locationname,'') as name,
  description,
  (ll.attrs->>'name') as communityname,
  (ll.attrs->>'code') as communitycode,
  ll.name as localityname,
  lm.name as mapareaname,
  ls.name as sectorname,
  (to_uniquebid(l.extid)).building as buildingnumber,
  (to_uniquebid(l.extid)).floor as floornumber,
  st_y(global_pos)::varchar as latitude,
  st_x(global_pos)::varchar as longitude
from
  location l
join
  locationhierarchy ls on l.locationhierarchy_uuid = ls.uuid
join
  locationhierarchy lm on ls.parent_uuid = lm.uuid
join
  locationhierarchy ll on lm.parent_uuid = ll.uuid
where
  not l.deleted;

-- sqlite schema requires non-null group name, drop
drop view v_socialgroup_sync;
create or replace view
  v_socialgroup_sync
as
select
  uuid,
  grouphead_uuid,
  location_uuid,
  coalesce(groupname,'') as groupname
from
  socialgroup
where
  not deleted
  and location_uuid is not null;
