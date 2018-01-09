alter table individual add column member_status varchar(255);

drop view v_individual_sync;
create view v_individual_sync as
select
  i.uuid,
  to_char(i.dob, 'YYYY-MM-DD') AS dob,
  i.extid,
  i.first_name AS firstname,
  i.gender,
  i.last_name AS lastname,
  i.home AS currentresidence,
  i.middle_name AS othernames,
  i.phone1 AS phonenumber,
  i.phone2 AS otherphonenumber,
  i.contact_name AS pointofcontactname,
  i.contact_phone AS pointofcontactphonenumber,
  i.language AS languagepreference,
  i.member_status AS memberstatus,
  i.nationality,
  i.dip AS otherid
from
  individual i
where
  not i.deleted
  and i.extid <> 'UNK'
  and i.home is not null;