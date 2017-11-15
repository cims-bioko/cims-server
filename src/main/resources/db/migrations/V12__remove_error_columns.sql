alter table error
  drop column deleted,
  drop column fieldworker_uuid,
  drop column assignedto,
  drop column resolutionstatus,
  drop column dateofresolution;

alter table error rename column datapayload to payload;
alter table error rename column errormessage to message;
