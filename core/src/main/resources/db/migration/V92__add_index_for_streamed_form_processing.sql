create index on form_submission ((date_trunc('hour', submitted)), collected, (processed is null));
