alter table users add column last_login_ts timestamp;
update users set last_login_ts = timestamp 'epoch' + last_login * interval '1 millisecond';
alter table users drop column last_login;
alter table users rename column last_login_ts to last_login;