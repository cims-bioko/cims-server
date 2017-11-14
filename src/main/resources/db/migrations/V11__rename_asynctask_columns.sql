alter table asynctask rename column taskstartdate to started;
alter table asynctask rename column taskenddate to finished;
alter table asynctask rename column taskname to name;
alter table asynctask rename column totalcount to itemcount;
alter table asynctask rename column md5hash to descriptor;
