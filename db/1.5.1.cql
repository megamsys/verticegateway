-- 1.5.1 upgrade only.
-- base.cql. 1.5.cql should be run already

use system_auth;

GRANT ALL PERMISSIONS  ON KEYSPACE vertice  TO vertadmin;

use vertice;

ALTER TABLE balances ADD biller_credit text;

CREATE TABLE IF NOT EXISTS events_for_marketplaces (id text, event_type text,  account_id text, marketplace_id text, data list<text>,json_claz text, created_at timestamp, PRIMARY KEY(account_id, created_at, marketplace_id, event_type))WITH CLUSTERING ORDER BY(created_at desc, marketplace_id desc, event_type desc);

CREATE TABLE IF NOT EXISTS launchptr (id text, account_id text, ptr long, created_at timestamp, PRIMARY KEY(account_id, id))
