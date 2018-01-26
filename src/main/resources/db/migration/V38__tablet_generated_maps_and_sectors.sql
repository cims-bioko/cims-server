create or replace function create_map(locality_uuid locationhierarchy.uuid%type, map_uuid locationhierarchy.uuid%type, map_name locationhierarchy.name%type) returns locationhierarchy.uuid%type as $$
declare
  locality locationhierarchy%rowtype;
  map_level_uuid locationhierarchylevel.uuid%type;
  map_extid locationhierarchy.extid%type;
  existing_map locationhierarchy%rowtype;
begin

  if not map_name ~ '^M\d{4}$' then
    raise exception '% is not a valid map name, must match M####', map_name;
  end if;

  -- find and store the one-and-only identified locality
  select * into strict locality from locationhierarchy lh join locationhierarchylevel lhl on lh.level = lhl.uuid where lhl.name = 'Locality' and lh.uuid = locality_uuid;
  raise notice 'found locality: %, %', locality.uuid, locality.name;

  if locality.name ~ '^Sin ' then
    raise exception 'creating map in % is not allowed', locality.name;
  end if;

  -- store the level uuid for maps
  select uuid into strict map_level_uuid from locationhierarchylevel where name = 'MapArea';
  raise notice 'found map level %', map_level_uuid;

  -- construct the map extid based on specified map name and locality
  map_extid := concat(map_name, '/', locality.name);

  -- raise an exception if it exists, otherwise create it
  begin
    select * into strict existing_map from locationhierarchy lh join locationhierarchylevel lhl on lh.level = lhl.uuid where lhl.name = 'MapArea' and lh.extid = map_extid;
    raise exception 'found existing map for extid %, %', map_extid, existing_map.uuid;
    return existing_map.uuid;
    exception
      when NO_DATA_FOUND then
        raise notice 'existing map with extid % not found, creating map: %', map_extid, map_uuid;
        insert into locationhierarchy values (map_uuid, concat(map_name, '/', locality.name), map_name, map_level_uuid, locality.uuid);
        return map_uuid;
      when TOO_MANY_ROWS then
        raise exception 'multiple maps with extid %', map_extid;
  end;

end$$ language plpgsql;


create or replace function create_sector(map_uuid locationhierarchy.uuid%type, sector_uuid locationhierarchy.uuid%type, sector_name locationhierarchy.name%type) returns locationhierarchy.uuid%type as $$
declare
  map locationhierarchy%rowtype;
  locality locationhierarchy%rowtype;
  sector_level_uuid locationhierarchylevel.uuid%type;
  sector_extid locationhierarchy.extid%type;
  existing_sector locationhierarchy%rowtype;
begin

  if not sector_name ~ '^S\d{3}$' then
    raise exception '% is not a valid sector name, must match S###', sector_name;
  end if;

  -- find and store the one-and-only identified map
  select * into strict map from locationhierarchy lh join locationhierarchylevel lhl on lh.level = lhl.uuid where lhl.name = 'MapArea' and lh.uuid = map_uuid;
  raise notice 'found map: %, %', map.uuid, map.name;

  -- find and store the one-and-only locality (the parent of the map)
  select * into strict locality from locationhierarchy lh join locationhierarchylevel lhl on lh.level = lhl.uuid where lhl.name = 'Locality' and lh.uuid = map.parent;
  raise notice 'found locality: %, %', locality.uuid, locality.name;

  if locality.name ~ '^Sin ' or map.name ~ '^Sin ' then
    raise exception 'creating sector in %, % is not allowed', locality.name, map.name;
  end if;

  -- store the level uuid for sectors
  select uuid into strict sector_level_uuid from locationhierarchylevel where name = 'Sector';
  raise notice 'found sector level %', sector_level_uuid;

  -- construct the sector extid based on specified sector name, map and locality
  sector_extid := concat(map.name, sector_name, '/', locality.name);

  -- return the existing sector if it exists, otherwise create it
  begin
    select * into strict existing_sector from locationhierarchy lh join locationhierarchylevel lhl on lh.level = lhl.uuid where lhl.name = 'Sector' and lh.extid = sector_extid;
    raise exception 'found existing sector for extid %, %', sector_extid, existing_sector.uuid;
    return existing_sector.uuid;
    exception
      when NO_DATA_FOUND then
        raise notice 'existing sector with extid % not found, creating sector: %', sector_extid, sector_uuid;
        insert into locationhierarchy values (sector_uuid, sector_extid, sector_name, sector_level_uuid, map.uuid);
        return sector_uuid;
      when TOO_MANY_ROWS then
        raise exception 'multiple sectors with extid %', sector_extid;
  end;

end$$ language plpgsql;
