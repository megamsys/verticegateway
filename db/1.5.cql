-- 1.5 upgradee only.
-- base.cql should be run already

use vertice;

CREATE TABLE IF NOT EXISTS billingtransactions(id text, account_id text, gateway text, amountin text, amountout text, fees text, tranid text, trandate text, currency_type text,  created_at text, json_claz text, PRIMARY KEY(account_id, trandate, created_at));

CREATE TABLE IF NOT EXISTS billedhistories (id text, account_id text, assembly_id text, bill_type text, billing_amount text, currency_type text, json_claz text, created_at text, PRIMARY KEY((account_id, assembly_id), bill_type, created_at));

CREATE TABLE IF NOT EXISTS balances (id text, account_id text, credit text, json_claz text, created_at text, updated_at text, PRIMARY KEY((account_id)));

CREATE TABLE IF NOT EXISTS sensors (id text,  account_id text, sensor_type text, assembly_id text, assembly_name text, assemblies_id text, node text, system text, status text, source text, message text, audit_period_beginning text, audit_period_ending text, audit_period_delta text, metrics list<text>, created_at text, PRIMARY KEY((account_id, assembly_id), sensor_type));

CREATE TABLE IF NOT EXISTS snapshots (snap_id text, asm_id text, org_id text, account_id text, name text, status text, image_id text, json_claz text, created_at text, PRIMARY KEY ((snap_id),account_id, asm_id));

CREATE TABLE IF NOT EXISTS events_for_vms (id text, event_type text,  account_id text, assembly_id text, data list<text>,json_claz text, created_at timestamp, PRIMARY KEY(account_id, created_at, assembly_id, event_type))WITH CLUSTERING ORDER BY(created_at desc, assembly_id desc, event_type desc);

CREATE TABLE IF NOT EXISTS events_for_containers (id text, event_type text, account_id text, assembly_id text, data list<text>, json_claz text, created_at timestamp, PRIMARY KEY(account_id, created_at, assembly_id, event_type))WITH CLUSTERING ORDER BY(created_at desc, assembly_id desc, event_type desc);

CREATE TABLE IF NOT EXISTS events_for_billings (event_type text, account_id text, assembly_id text, data list<text>, json_claz text, created_at text, PRIMARY KEY(account_id, created_at, assembly_id, event_type))WITH CLUSTERING ORDER BY(created_at desc, assembly_id desc, event_type desc);

CREATE TABLE IF NOT EXISTS events_for_storages (event_type text, account_id text, data list<text>, json_claz text, created_at text, PRIMARY KEY(account_id, created_at, event_type))WITH CLUSTERING ORDER BY(created_at desc, event_type desc);

CREATE TABLE IF NOT EXISTS subscriptions(id text, account_id text, model text, license text, trial_ends text, Json_claz text,created_at text,PRIMARY KEY(account_id));

CREATE TABLE IF NOT EXISTS disks (id text, disk_id  text, asm_id text, org_id text, account_id text, size text, status text, json_claz text, created_at text, PRIMARY KEY ((id), account_id, asm_id));

CREATE TABLE IF NOT EXISTS master_keys (id text, key text, created_at text, PRIMARY KEY ((id)));

CREATE TABLE IF NOT EXISTS licenses (id text, data text, created_at text, PRIMARY KEY ((id)));
