create table user_tokens (
  "user" varchar(32),
  token varchar(255),
  primary key("user", token)
);

alter table user_tokens add foreign key ("user") references users(uuid);
alter table user_tokens add foreign key (token) references access_token(value);