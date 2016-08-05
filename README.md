Vertice Gateway
================

API server for "[Megam vertice](https://www.megam.io)". The API server protectes the RESTful resources using [HMAC](http://www.ietf.org/rfc/rfc2104.txt) based authorization.

## Requirements


1. [SBT 0.13..11 >](https://scala-sbt.org)
2. [NSQ 0.3.7  ](http://nsq.io)
3. [OpenJDK 8.0](http://openjdk.java.net/install/index.html)
4. [Cassandra 3 +](http://apache.cassandra.org)


## Compile from source

You'll need `sbt` build tool. and `OpenJDK8.0`

```
* git clone https://github.com/megamsys/vertice_gateway.git

* cd vertice_gateway

* sbt

* clean

* compile

```

## Running

[Install Cassandra](http://cassandra.apache.org/download/)

[Install and start NSQ.io](http://nsq.io/deployment/installing.html)


```
* cd vertice_gateway

* sbt

* clean

* compile

* run

```

## Cassandra keyspace setup

```
* cd vertice_gateway/conf

* cqlsh -f vertice.cql

* cqlsh -f upgrade_1.5.cql

* cqlsh -f marketplaces.cql

```

## Type the url `http://localhost:9000`

You'll see this in your browser.

```json
{
  "status" : {
    "nsq" : "down"
  },
  "runtime" : {
    "total_mem" : "975 MB",
    "freemem" : "649 MB",
    "cores" : "4",
    "freespace" : "399 of 450 GB"
  }
}

```

Now you are all set.

# Contribution

# Documentation

For more info refer [documentation] (http://docs.megam.io)


# License

MIT


# Authors

Maintainers Megam (<info@megam.io>)
