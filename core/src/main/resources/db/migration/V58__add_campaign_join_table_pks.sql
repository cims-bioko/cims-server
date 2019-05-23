-- adds primary keys that should have been added during table creation
alter table campaign_devices add primary key (campaign, device);
alter table campaign_members add primary key (campaign, "user");
alter table campaign_forms add primary key (campaign, form_id, form_version);