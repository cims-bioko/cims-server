-- create function to update a backup
create or replace function update_backup(name text, new_name text, new_desc text) returns void as $$
declare
  safe_name text := quote_ident(name);
  safe_new_name text := quote_ident(new_name);
  schema_oid oid;
begin

  -- rename schema if name changed
  if name <> new_name then

    -- check that schema by original name exists and is a backup
    perform schema_oid from pg_namespace ns join backup b on ns.nspname = b.schema_name where b.schema_name = name;
    if not FOUND then
      raise exception 'backup % does not exist!', name;
    end if;

    -- check that a schema does not already exist with the new name
    perform nspname from pg_namespace where nspname = new_name;
    if FOUND then
      raise exception 'destination schema % already exists!', new_name;
    end if;

    -- rename the schema and update the backup record to match
    execute format('alter schema %s rename to %s', safe_name, safe_new_name);
    update backup set schema_name = new_name where schema_name = name;

  end if;

  -- always update comment
  execute format('comment on schema %s is %L', safe_new_name, new_desc);

end$$ language plpgsql;

create or replace function delete_backup(name text) returns void as $$
declare
  safe_name text := quote_ident(name);
  schema_oid oid;
begin

  -- check that schema by original name exists and is a backup
  perform schema_oid from pg_namespace ns join backup b on ns.nspname = b.schema_name where b.schema_name = name;
  if not FOUND then
    raise exception 'backup % does not exist!', name;
  end if;

  -- rename the schema and update the backup record to match
  execute format('drop schema %s cascade', safe_name);
  delete from backup where schema_name = name;

end$$ language plpgsql;