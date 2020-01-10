alter table form_submission add column campaign varchar(32) references campaign(uuid);
create index on form_submission(campaign);