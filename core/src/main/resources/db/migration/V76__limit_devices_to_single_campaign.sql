-- add column for temporarily disabling a campaign
drop table campaign_devices;

-- add column to assign a single campaign to a device
alter table device add column campaign varchar(32) references campaign(uuid);