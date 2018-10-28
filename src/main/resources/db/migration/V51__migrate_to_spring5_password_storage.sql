update
  cims.users
set
  password = concat('{noop}', password)
where
  password !~ '^{\w+}'
  and username <> 'system';