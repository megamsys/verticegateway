-- Upgrade your 1.0 schema to 1.5 level.

use vertice;

DROP TABLE IF EXISTS events;

DROP TABLE IF EXISTS billedhistories;

DROP TABLE IF EXISTS balances;

DROP TABLE IF EXISTS sensors;

DROP TABLE IF EXISTS accounts;

CREATE TABLE billedhistories (id text, account_id text, assembly_id text, bill_type text, billing_amount text, currency_type text, json_claz text, created_at text, PRIMARY KEY((account_id, assembly_id), bill_type, created_at));

CREATE TABLE balances (id text, account_id text, credit text, json_claz text, created_at text, updated_at text, PRIMARY KEY((account_id)));

CREATE TABLE sensors (id text,  account_id text, sensor_type text, assembly_id text, assembly_name text, assemblies_id text, node text, system text, status text, source text, message text, audit_period_beginning text, audit_period_ending text, audit_period_delta text, metrics list<text>, created_at text, PRIMARY KEY((account_id, assembly_id), sensor_type));

CREATE TABLE snapshots (snap_id text, asm_id text, org_id text, account_id text, name text, json_claz text, created_at text, PRIMARY KEY ((snap_id),account_id, asm_id));

CREATE TABLE events_for_vms (event_type text,  account_id text, assembly_id text, data list<text>,json_claz text, created_at text, PRIMARY KEY(account_id, created_at, assembly_id, event_type))WITH CLUSTERING ORDER BY(created_at desc, assembly_id desc, event_type desc);

CREATE TABLE billingtransactions (id text, account_id text, gateway text, amountin text, amountout text, fees text, tranid text, trandate text, currency_type text,  created_at text, json_claz text, PRIMARY KEY(account_id, trandate, created_at));

CREATE TABLE events_for_containers (event_type text, account_id text, assembly_id text, data list<text>, json_claz text, created_at text, PRIMARY KEY(account_id, created_at, assembly_id, event_type))WITH CLUSTERING ORDER BY(created_at desc, assembly_id desc, event_type desc);

CREATE TABLE events_for_billings (event_type text, account_id text, assembly_id text, data list<text>, json_claz text, created_at text, PRIMARY KEY(account_id, created_at, assembly_id, event_type))WITH CLUSTERING ORDER BY(created_at desc, assembly_id desc, event_type desc);

CREATE TABLE events_for_storages (event_type text, account_id text, data list<text>, json_claz text, created_at text, PRIMARY KEY(account_id, created_at, event_type))WITH CLUSTERING ORDER BY(created_at desc, event_type desc);

CREATE TABLE accounts( id text,  name text, phone text, email text,  api_key text, password text, states text, approval text, suspend text, registration_ip_address text, dates text, PRIMARY KEY (email));

CREATE TABLE subscriptions(id text, account_id text, model text, license text, trial_ends text, Json_claz text,created_at text,PRIMARY KEY(account_id));

 
