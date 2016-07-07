//cqlsh 103.56.92.24 -f $MEGAM_HOME/verticegateway/vertice.cql

CREATE KEYSPACE IF NOT EXISTS vertice
  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }
 AND DURABLE_WRITES = false;

use vertice;

CREATE TABLE accounts( id text,  first_name text, last_name text,  phone text, email text,  api_key text, password text, authority text, password_reset_key text, password_reset_sent_at text, status text, created_at text, PRIMARY KEY (email));

CREATE TABLE assemblies (id text, org_id text, name text, assemblies list<text>, inputs list<text>, json_claz text, created_at text, PRIMARY KEY ((org_id), id));

CREATE TABLE assembly (id text, org_id text, account_id text, name text, components list<text>, tosca_type text, policies list<text>, inputs list<text>, outputs list<text>, status text, json_claz text, created_at text, PRIMARY KEY ((org_id), id));

CREATE TABLE components (id text, name text, tosca_type text, inputs list<text>, outputs list<text>, envs list<text>, artifacts text, related_components list<text>, operations list<text>, repo text, status text, json_claz text, created_at text, PRIMARY KEY (id));

CREATE TABLE settings(name text, scylla text, home text, api text, nsqd text, org text, domain text, route53_access_key text, route53_secret_key text, docker_swarm text, one_endpoint text, one_userid text, one_password text, primary key(name));

CREATE TABLE organizations(id text, accounts_id text, name text, json_claz text, created_at  text, PRIMARY KEY ((accounts_id), id)  );

CREATE TABLE domains (id text, org_id text,  name text, json_claz text, created_at text, PRIMARY KEY((org_id), name));

CREATE TABLE sshkeys (id text, org_id text, name text, privatekey text, publickey text, json_claz text, created_at text, PRIMARY KEY((org_id), name));

CREATE TABLE requests( id text, cat_id text, cattype text,  name text,  action text, category text, created_at text, PRIMARY KEY (id));

CREATE TABLE marketplaces ( settings_name text,  cattype text, flavor text,  image text, catorder text, url text, json_claz text, envs list<text>,  options list<text>, plans map <text, text>,  PRIMARY KEY ((settings_name), flavor) );

CREATE TABLE events (event_type text,  account_id text, assembly_id text, data list<text>, created_at text, PRIMARY KEY((account_id), assembly_id, event_type,  created_at));

CREATE TABLE addons (id text, provider_id text, account_id text, provider_name text, options list<text>, created_at text, PRIMARY KEY ((account_id), provider_name);
