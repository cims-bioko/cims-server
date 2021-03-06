update residency set insertdate = startdate;

create or replace view
  v_individual_max_residency
as
select distinct on (individual_uuid)
  individual_uuid,
  uuid
from
  residency
where
  not deleted
order by
  individual_uuid, insertdate desc;

create or replace view
  v_individual_sync
as
select
  i.uuid as uuid,
  to_char(i.dob,'YYYY-MM-DD') as dob,
  i.extId as extId,
  'Unknown Individual'::varchar(32) as father,
  case i.firstName when 'null' then null else i.firstName end as firstName,
  i.gender as gender,
  case i.lastName when 'null' then null else i.lastName end as lastName,
  'Unknown Individual'::varchar(32) as mother,
  rl.uuid as currentResidence,
  'NA'::varchar(255) as endType,
  case i.middleName when 'null' then null else i.middleName end as otherNames,
  (case when dob > now() - interval '1 year' then date_part('month',age(i.dob)) else date_part('year', age(i.dob)) end)::int as age,
  (case when i.dob > now() - interval '1 year' then 'Months' else 'Years' end)::varchar(32) as ageUnits,
  case i.phoneNumber when 'null' then null else i.phoneNumber end as phoneNumber,
  case i.otherPhoneNumber when 'null' then null else i.otherPhoneNumber end as otherPhoneNumber,
  case i.pointOfContactName when 'null' then null else i.pointOfContactName end as pointOfContactName,
  case i.pointOfContactPhoneNumber when 'null' then null else i.pointOfContactPhoneNumber end as pointOfContactPhoneNumber,
  i.languagePreference as languagePreference,
  'Unknown'::varchar as memberStatus,
  case i.nationality when 'null' then null else i.nationality end as nationality,
  case i.dip when 0 then null else i.dip end as otherId
from
  individual i
left join
  v_individual_max_residency mr on i.uuid = mr.individual_uuid
left join
  residency r on mr.uuid = r.uuid
left join
  location rl on r.location_uuid = rl.uuid
where
  not i.deleted
  and i.extId <> 'UNK'
  and r.uuid is not null;

alter table residency drop column enddate, drop column endtype, drop column startdate, drop column starttype;
