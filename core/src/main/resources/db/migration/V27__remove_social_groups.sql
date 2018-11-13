-- add columns to keep data that will remain
alter table individual add home varchar(32) references location(uuid);
alter table individual add home_role varchar(255);

-- populate them based on group/membership data
update
  individual i
set
  home = g.location,
  home_role = m.role
from
  membership m,
  socialgroup g
where
  i.uuid = m.member
  and g.uuid = m.group;

-- drop the sync views, we're not going to use them
drop view v_membership_sync;
drop view v_socialgroup_sync;

-- change the individual sync view to not reference group/membership data
create or replace view v_individual_sync as
select
  i.uuid,
  to_char(i.dob, 'YYYY-MM-DD') AS dob,
  i.extid,
  'Unknown Individual'::varchar AS father,
  i.first_name AS firstname,
  i.gender,
  i.last_name AS lastname,
  'Unknown Individual'::varchar AS mother,
  i.home AS currentresidence,
  'NA'::varchar(255) AS endtype,
  i.middle_name AS othernames,
  case when i.dob > (now() - '1 year'::interval) then date_part('month', age(i.dob)) else date_part('year', age(i.dob))end::integer AS age,
  case when i.dob > (now() - '1 year'::interval) then 'Months' else 'Years' end AS ageunits,
  i.phone1 AS phonenumber,
  i.phone2 AS otherphonenumber,
  i.contact_name AS pointofcontactname,
  i.contact_phone AS pointofcontactphonenumber,
  i.language AS languagepreference,
  'Unknown'::character varying AS memberstatus,
  i.nationality,
  i.dip AS otherid
from
  individual i
where
  not i.deleted
  and i.extid <> 'UNK'
  and i.home is not null;

-- eliminate the tables
drop table membership;
drop table socialgroup;