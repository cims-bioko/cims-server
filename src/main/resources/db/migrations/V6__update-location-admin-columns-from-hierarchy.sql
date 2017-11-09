create or replace function update_location_admin_columns_from_hierarchy() RETURNS trigger AS $$
begin
  select
    lr.name, lp.name, ld.name, lsd.name, ll.name, lm.name, ls.name
  into
    new.regionname, new.provincename, new.districtname, new.subdistrictname, new.localityname, new.mapareaname, new.sectorname
  from
    locationhierarchy ls
  join
    locationhierarchy lm on ls.parent_uuid = lm.uuid
  join
    locationhierarchy ll on lm.parent_uuid = ll.uuid
  join
    locationhierarchy lsd on ll.parent_uuid = lsd.uuid
  join
    locationhierarchy ld on lsd.parent_uuid = ld.uuid
  join
    locationhierarchy lp on ld.parent_uuid = lp.uuid
  join
    locationhierarchy lr on lp.parent_uuid = lr.uuid
  where
    ls.uuid = new.locationhierarchy_uuid;

  return new;

end$$ LANGUAGE plpgsql;

create trigger update_admin_columns
before update or insert on location for each row
execute procedure update_location_admin_columns_from_hierarchy();