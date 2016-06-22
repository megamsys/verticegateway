
use vertice;

DROP  TABLE events;

CREATE TABLE snapshots (snap_id text, asm_id text, org_id text, account_id text, name text, json_claz text, created_at text, PRIMARY KEY ((snap_id),account_id, asm_id));

CREATE TABLE events_for_vms (event_type text,  account_id text, assembly_id text, data list<text>,json_claz text, created_at text, PRIMARY KEY(account_id, created_at, assembly_id, event_type))WITH CLUSTERING ORDER BY(created_at desc, assembly_id desc, event_type desc);
