-- add column for marking the default campaign
alter table campaign add "default" boolean not null default false;

-- only allow one row to be marked default
create unique index on campaign("default") where "default" = true;
