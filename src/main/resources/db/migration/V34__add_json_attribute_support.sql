alter table individual add column attrs jsonb;
create index on individual using gin (attrs);

alter table location add column attrs jsonb;
create index on location using gin (attrs);

create or replace view v_individual_sync as
select
  i.uuid,
  to_char(i.dob, 'YYYY-MM-DD') AS dob,
  i.extid,
  i.first_name AS firstname,
  i.gender,
  i.last_name AS lastname,
  i.home AS currentresidence,
  i.home_role AS relationshiptohead,
  i.middle_name AS othernames,
  i.phone1 AS phonenumber,
  i.phone2 AS otherphonenumber,
  i.contact_name AS pointofcontactname,
  i.contact_phone AS pointofcontactphonenumber,
  i.language AS languagepreference,
  i.status,
  i.nationality,
  i.dip AS otherid,
  attrs::text as attrs
from
  individual i
where
  not i.deleted
  and i.extid <> 'UNK'
  and i.home is not null;

create or replace view v_location_sync as
select
  l.uuid,
  l.extid,
  l.hierarchy as hierarchyuuid,
  ls.extid as hierarchyextid,
  coalesce(l.name,'') as name,
  description,
  (ll.attrs->>'name') as communityname,
  (ll.attrs->>'code') as communitycode,
  ll.name as localityname,
  lm.name as mapareaname,
  ls.name as sectorname,
  (to_uniquebid(l.extid)).building as buildingnumber,
  (to_uniquebid(l.extid)).floor as floornumber,
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

create or replace view v_locationhierarchy_sync as
select
  lh.uuid,
  lh.extid,
  lh.name,
  l.name AS level,
  lh.parent,
  lh.attrs::text as attrs
from locationhierarchy lh
join locationhierarchylevel l on lh.level = l.uuid;