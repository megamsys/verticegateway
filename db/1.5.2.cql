-- 1.5.2 upgrade only.
-- base.cql. 1.5.cql, 1.5.1.cql should be run already

use system_auth;

GRANT ALL PERMISSIONS  ON KEYSPACE vertice  TO vertadmin;

use vertice;

CREATE TABLE IF NOT EXISTS flavors (id text, name text, cpu text, ram text, disk text, category list<text>, regions list<text>, price list<text>, properties list<text>, status text, json_claz text, created_at timestamp, updated_at timestamp, PRIMARY KEY(name, id));
