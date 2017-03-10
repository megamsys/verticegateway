-- 1.5 upgradee only.
-- base.cql should be run already

use system_auth;

GRANT ALL PERMISSIONS  ON KEYSPACE vertice  TO vertadmin;

use vertice;

ALTER TABLE balances ADD biller_credit text;
