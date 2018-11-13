alter table error drop column errorlog_uuid;
alter table errorlog drop column error_uuid;
alter table errorlog add column errormessage varchar(255);
update errorlog el set errormessage = e.errormessage from error e where e.error_uuid = el.uuid;
drop table error;
alter table errorlog rename to error;