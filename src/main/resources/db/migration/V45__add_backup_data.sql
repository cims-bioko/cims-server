-- add table to track schema objects created by function
create table backup (
  schema_name text primary key,
  created timestamp not null default current_timestamp
);

-- create view to display backup information
create view v_backup as
select
  b.schema_name, d.description, b.created
from
  backup b
join
  pg_namespace ns on b.schema_name = ns.nspname
join
  pg_description d on ns.oid = d.objoid;

-- create function to create a backup
create or replace function backup_data(src text, dst text, dsc text) returns void as $$
declare
  orig_search_path text := current_setting('search_path');
  src_quoted text := quote_ident(src);
  dst_quoted text := quote_ident(dst);
  src_oid oid;
begin

  -- confirm source exists
  select oid into src_oid from pg_namespace where nspname = src_quoted;
  if not FOUND then
    raise exception 'source schema % does not exist!', src;
  end if;

  -- confirm destination does not exist
  perform nspname from pg_namespace where nspname = dst_quoted;
  if FOUND then
    raise exception 'destination schema % already exists!', dst;
  end if;

  -- create the destination schema with description as comment
  execute concat('create schema ', dst_quoted);
  execute concat('comment on schema ', dst_quoted, ' is ', quote_literal(dsc));

  -- record that we created the object as a backup
  insert into backup (schema_name) values (dst_quoted);

  -- prepend destination to search path so pg functions return schema-qualified definitions
  perform set_config('search_path', concat(dst_quoted, ', ', orig_search_path), true);

  -- copy the tables
  declare
    tbl_info record;
  begin
    for tbl_info in
      select
        table_name as dst, concat(table_schema, '.', table_name) as src
      from
        information_schema.tables
      where
        table_schema = src_quoted
        and table_type = 'BASE TABLE'
    loop
      execute concat('create table ', tbl_info.dst, ' (like ', tbl_info.src, ' including all)');
      execute concat('insert into ', tbl_info.dst, ' select * from ', tbl_info.src);
      execute concat('analyze ', tbl_info.dst); -- help optimizer, stats will be wrong
    end loop;
  end;

  -- copy the constraints
  declare
    fk_info record;
  begin
    for fk_info in
      select
        rn.relname as tbl,
        ct.conname as name,
        pg_get_constraintdef(ct.oid) as def
      from
        pg_constraint ct
      join
        pg_class rn on rn.oid = ct.conrelid
      where
        connamespace = src_oid
        and rn.relkind = 'r'
        and ct.contype = 'f'
    loop
      execute concat('alter table ', quote_ident(fk_info.tbl), ' add constraint ', quote_ident(fk_info.name), ' ',
                     replace(fk_info.def, src, dst));
    end loop;
  end;

  -- copy the views
  declare
    vw_info record;
  begin
    for vw_info in
      select
        table_name as name,
        view_definition as def
      from
        information_schema.views
      where
        table_schema = src_quoted
    loop
      execute concat('create or replace view ', vw_info.name, ' as ', replace(vw_info.def, src, dst));
    end loop;
  end;

  -- restore search path so subsequent operations sharing the tx operate as expected
  perform set_config('search_path', orig_search_path, false);

end$$ language plpgsql;