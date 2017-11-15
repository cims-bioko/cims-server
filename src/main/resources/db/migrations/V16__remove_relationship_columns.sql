update relationship set insertdate = startdate;

create or replace view
  v_relationship_sync
as
select
  uuid,
  individualA_uuid as individualA,
  individualB_uuid as individualB,
  aIsToB as relationshipType,
  to_char(insertDate,'YYYY-MM-DD') as startDate
from
  relationship
where
  not deleted;

alter table relationship drop column enddate, drop column endtype, drop column startdate;
