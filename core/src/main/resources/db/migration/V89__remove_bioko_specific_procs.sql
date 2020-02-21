-- drops objects so new deployments don't start with these (bioko will need to backup and restore to keep running)
drop function create_map(locality_uuid locationhierarchy.uuid%type, map_name locationhierarchy.name%type);
drop function create_map(locality_uuid locationhierarchy.uuid%type, map_uuid locationhierarchy.uuid%type, map_name locationhierarchy.name%type);
drop function create_sector(map_uuid locationhierarchy.uuid%type, sector_name locationhierarchy.name%type);
drop function create_sector(map_uuid locationhierarchy.uuid%type, sector_uuid locationhierarchy.uuid%type, sector_name locationhierarchy.name%type);
drop function to_uniquebid(str text);