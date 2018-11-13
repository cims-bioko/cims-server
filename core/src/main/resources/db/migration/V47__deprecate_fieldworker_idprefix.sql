create or replace view v_fieldworker_sync as
select
  uuid,
  extid,
  0 as idprefix,
  first_name as firstname,
  last_name as lastname,
  password_hash as password
from
  fieldworker
where
  deleted is null
  and extid <> 'UNK';

comment on view v_fieldworker_sync is 'used to generate mobile fieldworkers table';

alter table fieldworker drop column idprefix;