-- 2.0 upgradee only.
-- base.cql should be run already

use system_auth;

GRANT ALL PERMISSIONS  ON KEYSPACE vertice  TO vertadmin;

use vertice;


CREATE TABLE IF NOT EXISTS credits (id text, account_id text, credit text, json_claz text, created_at timestamp, PRIMARY KEY((account_id), created_at));
