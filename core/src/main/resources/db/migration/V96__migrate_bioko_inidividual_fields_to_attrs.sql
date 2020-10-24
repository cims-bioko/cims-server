/* migrate bioko specific columns to an attribute */
update individual set attrs = coalesce(attrs, '{}'::jsonb) || jsonb_build_object('dip', dip) where dip is not null and dip != 0;
update individual set attrs = coalesce(attrs, '{}'::jsonb) || jsonb_build_object('home_role', home_role) where home_role is not null;
update individual set attrs = coalesce(attrs, '{}'::jsonb) || jsonb_build_object('status', status) where status is not null;
update individual set attrs = coalesce(attrs, '{}'::jsonb) || jsonb_build_object('language', language) where language is not null;
update individual set attrs = coalesce(attrs, '{}'::jsonb) || jsonb_build_object('nationality', nationality) where nationality is not null;
update individual set attrs = coalesce(attrs, '{}'::jsonb) || jsonb_build_object('phone1', phone1) where phone1 is not null and phone1 != '';
update individual set attrs = coalesce(attrs, '{}'::jsonb) || jsonb_build_object('phone2', phone2) where phone2 is not null and phone2 != '';
update individual set attrs = coalesce(attrs, '{}'::jsonb) || jsonb_build_object('contact_name', contact_name) where contact_name is not null;
update individual set attrs = coalesce(attrs, '{}'::jsonb) || jsonb_build_object('contact_phone', contact_phone) where contact_phone is not null and contact_phone != '';

/* update the v_individual_sync so it uses the attributes */
drop view v_individual_sync;
create view v_individual_sync as
select
  uuid,
  to_char(dob, 'YYYY-MM-DD') AS dob,
  extid,
  first_name as firstname,
  gender,
  last_name as lastname,
  home as currentresidence,
  attrs->>'home_role' as relationshiptohead,
  middle_name as othernames,
  attrs->>'phone1' as phonenumber,
  attrs->>'phone2' as otherphonenumber,
  attrs->>'contact_name' as pointofcontactname,
  attrs->>'contact_phone' as pointofcontactphonenumber,
  attrs->>'language' as languagepreference,
  attrs->>'status' as status,
  attrs->>'nationality' as nationality,
  attrs->>'dip' as otherid,
  attrs::text AS attrs
from
  individual
where
  deleted is null
  and extid <> 'UNK'
  and home is not null;

/* drop the columns after we have migrated */
alter table individual drop dip;
alter table individual drop home_role;
alter table individual drop status;
alter table individual drop language;
alter table individual drop nationality;
alter table individual drop phone1;
alter table individual drop phone2;
alter table individual drop contact_name;
alter table individual drop contact_phone;