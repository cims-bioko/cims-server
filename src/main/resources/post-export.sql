create index individual_extid_index on individuals(extid)
create index individual_residency_index on individuals(currentresidence)
create index location_hierarchy_uuid_index on locations(hierarchyuuid)
create index locationhierarchy_extid_index on hierarchyitems(extid)
create index fieldworkers_extid_index on fieldworkers(extid)
create index fieldworkers_password_index on fieldworkers(password)
vacuum
