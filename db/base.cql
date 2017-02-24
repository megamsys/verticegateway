-- Base schema
-- Beware: This drops your keyspace and creates a new one.

DROP KEYSPACE IF EXISTS vertice;

-- 1. Default - non repicated setup.
CREATE KEYSPACE IF NOT EXISTS vertice
  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }
 AND DURABLE_WRITES = false;

-- 2. Replicated setup
-- uncomment the below and comment 1. for a replicated setup.
-- CREATE KEYSPACE IF NOT EXISTS vertice
-- WITH REPLICATION = { 'class' : 'NetworkTopologyStrategy', 'dc1' : 2 , 'dc2' : 2 }
-- AND DURABLE_WRITES = false;

-- setting username & password for cassandra
-- 1. alter system keyspace replication_factor
alter KEYSPACE system_auth WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '2'}  AND durable_writes = true;

-- 2. create new user and give the modify permission to user
use system_auth;
INSERT INTO roles (role , can_login , is_superuser , member_of , salted_hash ) VALUES ( 'vertadmin',True,True, null, '$2a$10$p7I2t58GhZ9ZvqeCY2sbkehhQhqZ1/BlNuBp2bD7HY5CfGF9OI0uS');
INSERT INTO role_permissions (role , resource , permissions ) VALUES ( 'vertadmin', '<keyspace vertice>', {'MODIFY'});

use vertice;

CREATE TABLE accounts( id text,  name text, phone text, email text,  api_key text, password text, states text, approval text, suspend text, registration_ip_address text, dates text, PRIMARY KEY (email));

CREATE TABLE assemblies (id text, org_id text, name text, assemblies list<text>, inputs list<text>, json_claz text, created_at timestamp, PRIMARY KEY ((org_id),created_at, id));

CREATE TABLE assembly (id text, org_id text, account_id text, name text, components list<text>, tosca_type text, policies list<text>, inputs list<text>, outputs list<text>, status text, state text, json_claz text, created_at timestamp, PRIMARY KEY ((org_id),created_at, id));

CREATE TABLE components (id text, org_id text, name text, tosca_type text, inputs list<text>, outputs list<text>, envs list<text>, artifacts text, related_components list<text>, operations list<text>, repo text, status text, state text, json_claz text, created_at timestamp, PRIMARY KEY ((org_id), id));

CREATE TABLE organizations(id text, accounts_id text, name text, json_claz text, created_at  timestamp, PRIMARY KEY ((accounts_id), id));

CREATE TABLE domains (id text, org_id text,  name text, json_claz text, created_at timestamp, PRIMARY KEY((org_id), name));

CREATE TABLE sshkeys (id text, org_id text, name text, privatekey text, publickey text, json_claz text, created_at timestamp, PRIMARY KEY((org_id), name));

CREATE TABLE requests( id text, account_id text, cat_id text, cattype text,  name text,  action text, category text, created_at timestamp, PRIMARY KEY ((account_id),id));

CREATE TABLE marketplaces ( id text, flavor text,  provided_by text, cattype text, catorder text,  status text, image text, url text, envs list<text>, options list<text>, inputs list<text>, outputs list<text>, acl_policies list<text>, plans list<text>, json_claz text, updated_at timestamp, created_at timestamp, PRIMARY KEY ((flavor), provided_by));

CREATE TABLE addons (id text, provider_id text, account_id text, provider_name text, options list<text>, json_claz text, created_at timestamp, PRIMARY KEY ((account_id), provider_name));
