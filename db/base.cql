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

use vertice;

CREATE TABLE accounts( id text,  name text, phone text, email text,  api_key text, password text, states text, approval text, suspend text, registration_ip_address text, dates text, PRIMARY KEY (email));

CREATE TABLE assemblies (id text, org_id text, name text, assemblies list<text>, inputs list<text>, json_claz text, created_at text, PRIMARY KEY ((org_id), id));

CREATE TABLE assembly (id text, org_id text, account_id text, name text, components list<text>, tosca_type text, policies list<text>, inputs list<text>, outputs list<text>, status text, state text, json_claz text, created_at text, PRIMARY KEY ((org_id), id));

CREATE TABLE components (id text, name text, tosca_type text, inputs list<text>, outputs list<text>, envs list<text>, artifacts text, related_components list<text>, operations list<text>, repo text, status text, state text, json_claz text, created_at text, PRIMARY KEY (id));

CREATE TABLE organizations(id text, accounts_id text, name text, json_claz text, created_at  text, PRIMARY KEY ((accounts_id), id)  );

CREATE TABLE domains (id text, org_id text,  name text, json_claz text, created_at text, PRIMARY KEY((org_id), name));

CREATE TABLE sshkeys (id text, org_id text, name text, privatekey text, publickey text, json_claz text, created_at text, PRIMARY KEY((org_id), name));

CREATE TABLE requests( id text, cat_id text, cattype text,  name text,  action text, category text, created_at text, PRIMARY KEY (id));

CREATE TABLE marketplaces ( settings_name text,  cattype text, flavor text,  image text, catorder text, url text, json_claz text, envs list<text>,  options list<text>, plans map <text, text>,  PRIMARY KEY ((settings_name), flavor) );

CREATE TABLE addons (id text, provider_id text, account_id text, provider_name text, options list<text>, json_claz text, created_at text, PRIMARY KEY ((account_id), provider_name));
