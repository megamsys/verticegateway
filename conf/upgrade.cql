use vertice;

Drop table events;

create table snapshots (snap_id text, asm_id text, org_id text, account_id text, name text, json_claz text, created_at text, PRIMARY KEY ((snap_id),account_id, asm_id));

CREATE TABLE events_for_vms (event_type text,  account_id text, assembly_id text, data list<text>,json_claz text, created_at text, PRIMARY KEY(account_id, created_at, assembly_id, event_type))WITH CLUSTERING ORDER BY(created_at desc, assembly_id desc, event_type desc);

CREATE TABLE billingtranscations (id text, account_id text, gateway text, amountin text, amountout text, fees text, tranid text, trandate text, currency_type text,  created_at text, json_claz text, PRIMARY KEY(account_id, trandate, created_at));
