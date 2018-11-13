alter table error rename column insertdate to created;

alter table fieldworker rename column firstname to first_name;
alter table fieldworker rename column lastname to last_name;
alter table fieldworker rename column passwordhash to password_hash;
alter table fieldworker rename column insertdate to created;

alter table individual rename column firstname to first_name;
alter table individual rename column lastname to last_name;
alter table individual rename column middlename to middle_name;
alter table individual rename column phonenumber to phone1;
alter table individual rename column otherphonenumber to phone2;
alter table individual rename column languagepreference to language;
alter table individual rename column pointofcontactname to contact_name;
alter table individual rename column pointofcontactphonenumber to contact_phone;
alter table individual rename column insertdate to created;
alter table individual rename column collectedby_uuid to collector;

alter table location rename column locationname to name;
alter table location rename column locationtype to type;
alter table location rename column locationhierarchy_uuid to hierarchy;
alter table location rename column collectedby_uuid to collector;
alter table location rename column insertdate to created;

alter table locationhierarchy rename column level_uuid to level;
alter table locationhierarchy rename column parent_uuid to parent;

alter table locationhierarchylevel rename column keyidentifier to keyid;

alter table membership rename column bistoa to role;
alter table membership rename column individual_uuid to member;
alter table membership rename column socialgroup_uuid to "group";
alter table membership rename column collectedby_uuid to collector;
alter table membership rename column insertdate to created;

alter table role rename column insertdate to created;

alter table socialgroup rename column groupname to name;
alter table socialgroup rename column grouptype to type;
alter table socialgroup rename column location_uuid to location;
alter table socialgroup rename column grouphead_uuid to head;
alter table socialgroup rename column collectedby_uuid to collector;
alter table socialgroup rename column insertdate to created;

alter table role_privileges rename column role_uuid to role;
alter table role_privileges rename column privilege_uuid to privilege;

alter table user_roles rename column user_uuid to "user";
alter table user_roles rename column role_uuid to role;

alter table users rename column lastlogintime to last_login;
alter table users rename column firstname to first_name;
alter table users rename column lastname to last_name;
alter table users rename column fullname to full_name;

