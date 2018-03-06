-- make deleted columns nullable
alter table location alter deleted drop not null;
alter table fieldworker alter deleted drop not null;
alter table individual alter deleted drop not null;
alter table role alter deleted drop not null;
alter table users alter deleted drop not null;

-- drop views that depend on deleted columns
drop view v_location_sync;
drop view v_fieldworker_sync;
drop view v_individual_sync;

-- convert deleted columns to timestamp data type
alter table location alter deleted type timestamp using case deleted when true then now() else null end;
alter table fieldworker alter deleted type timestamp using case deleted when true then now() else null end;
alter table individual alter deleted type timestamp using case deleted when true then now() else null end;
alter table users alter deleted type timestamp using case deleted when true then now() else null end;
alter table role alter deleted type timestamp using case deleted when true then now() else null end;

-- recreate views after type change

create view v_location_sync as
select
  l.uuid,
  l.extid,
  hierarchy as hierarchyuuid,
  coalesce(l.name, '') AS name,
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

create view v_fieldworker_sync as
select
  uuid,
  extid,
  idprefix,
  first_name as firstname,
  last_name as lastname,
  password_hash as password
from
  fieldworker
where
  deleted is null
  and extid <> 'UNK';

create view v_individual_sync as
select
  uuid,
  to_char(dob, 'YYYY-MM-DD') AS dob,
  extid,
  first_name as firstname,
  gender,
  last_name as lastname,
  home as currentresidence,
  home_role as relationshiptohead,
  middle_name as othernames,
  phone1 as phonenumber,
  phone2 as otherphonenumber,
  contact_name as pointofcontactname,
  contact_phone as pointofcontactphonenumber,
  language as languagepreference,
  status,
  nationality,
  dip as otherid,
  attrs::text AS attrs
from
  individual
where
  deleted is null
  and extid <> 'UNK'
  and home is not null;

-- re-add comments since they were dropped
comment on view v_location_sync is 'used to generate mobile locations table';
comment on view v_fieldworker_sync is 'used to generate mobile fieldworkers table';
comment on view v_individual_sync is 'used to generate mobile individuals table';

