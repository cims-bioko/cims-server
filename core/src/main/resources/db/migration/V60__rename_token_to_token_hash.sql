-- rename the token field to reflect that it is actually a hashed value
alter table device rename column token to token_hash;