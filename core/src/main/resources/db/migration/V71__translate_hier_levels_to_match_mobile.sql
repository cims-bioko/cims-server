-- translate hierarchy levels to mobile's convention by making initial letter lowercase
create or replace view v_locationhierarchy_sync as
select
  lh.uuid,
  lh.extid,
  lh.name,
  concat(lower(substring(l.name, 1, 1)), substring(l.name, 2))::varchar(255) as level,
  lh.parent,
  lh.attrs::text as attrs
from locationhierarchy lh
join locationhierarchylevel l on lh.level = l.uuid;