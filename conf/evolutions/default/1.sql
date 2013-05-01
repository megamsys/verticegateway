# Account schema migration
 
# --- !Ups

CREATE TABLE account (
    id         integer NOT NULL PRIMARY KEY,
    email      text NOT NULL UNIQUE,
    secret     text NOT NULL,
    name       text NOT NULL,
    permission text NOT NULL
);

# --- !Downs

DROP TABLE account;

