-- move the interesting type data from the type column to a type attribute
update
  location
set
  attrs = coalesce(attrs, jsonb '{}') || jsonb_build_object('type', type)
where
  type is not null
  and type <> 'Household';

-- drop the type column
alter table location drop column type;