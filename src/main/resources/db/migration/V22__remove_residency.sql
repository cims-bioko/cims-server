create or replace function uuid_nodash() returns varchar(32) AS $$
declare
  uuid text = uuid_generate_v4();
begin
  return replace(uuid, '-', '');
end$$ language plpgsql;

/*
   The only individuals with non-matching residency/socialgroups are one without a socialgroup.
   Since that is the case, we can create the missing social groups based on their residency records.
   Generate social groups and memberships to patch up the gaps.
 */
do $$
declare
  idv individual%rowtype;
  res residency%rowtype;
  loc location%rowtype;
  groupid socialgroup.uuid%type;
begin
  for idv in
    select * from individual i
    where not deleted and not exists (
      select 1 from membership m join socialgroup s on m.socialgroup_uuid = s.uuid
      where m.individual_uuid = i.uuid and not m.deleted and not s.deleted)
    and exists (
      select 1 from residency r where r.individual_uuid = i.uuid and not r.deleted)
  loop

    -- get the individual's residency (required)
    select * into strict res from residency
    where individual_uuid = idv.uuid and not deleted;

    -- get the location for that residency (required)
    select * into strict loc from location
    where uuid = res.location_uuid and not deleted;

    -- get existing group for the location id and name (usually group head's lastname) or create it
    begin
      select uuid into strict groupid from socialgroup
      where location_uuid = loc.uuid and groupname = loc.locationname and not deleted;
      exception
        when NO_DATA_FOUND then
          select uuid_nodash() into strict groupid;
          insert into socialgroup
            (uuid, deleted, insertdate, extid, groupname, grouptype, location_uuid, collectedby_uuid, grouphead_uuid)
          values
            (groupid, false, now(), loc.extid, idv.lastname, 'COH', loc.uuid, 'UnknownFieldWorker', idv.uuid);
        when TOO_MANY_ROWS then
          raise exception 'multiple groups for location uuid=%,name=%', loc.uuid, loc.locationname;
    end;

    -- finally, make the individual a member of the social group (with unknown relationship to head)
    insert into membership
      (uuid, deleted, insertdate, bistoa, collectedby_uuid, individual_uuid, socialgroup_uuid)
    values
      (uuid_nodash(), false, now(), 9, 'UnknownFieldWorker', idv.uuid, groupid);

  end loop;
end$$;

/* redefine individual view to use social group location to determine last residence */
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
  i.uuid as uuid,
  to_char(i.dob,'YYYY-MM-DD') as dob,
  i.extId as extId,
  'Unknown Individual'::varchar(32) as father,
  case i.firstName when 'null' then null else i.firstName end as firstName,
  i.gender as gender,
  case i.lastName when 'null' then null else i.lastName end as lastName,
  'Unknown Individual'::varchar(32) as mother,
  lr.location_uuid as currentResidence,
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
  last_residence lr on i.uuid = lr.individual_uuid
where
  not i.deleted
  and i.extId <> 'UNK'
  and lr.location_uuid is not null;

/* drop the residency view, since it was only for the view we just redefined */
drop view v_individual_max_residency;

/* finally, drop the residency table itself */
drop table residency;
