Vertice Gateway
================

API server for "[MegamVertice](https://www.megam.io)" `1.5.x or < 2.0`.

API server for 2.0 is based on rust and will be release shortly.

The API server protects the RESTful resources using

- [HMAC](http://www.ietf.org/rfc/rfc2104.txt) based authorization.
- PASSWORD based on PBKDF2
- AuthAuth using a master key

## Requirements


1. [SBT 0.13.11 >](https://scala-sbt.org)
2. [NSQ 0.3.x  ](http://nsq.io)
3. [OpenJDK 8.0](http://openjdk.java.net/install/index.html)
4. [Cassandra 3 +](http://apache.cassandra.org)


## Compile from source

You'll need `sbt` build tool. and `OpenJDK8.0`

### Fork

After you have forked a copy of https://github.com/megamsys/vertice_gateway.git

### Steps

```

* git clone https://github.com/<your_github_id>/vertice_gateway.git

* cd vertice_gateway

* sbt

* clean

* compile


```

## Running

[Install Cassandra](http://cassandra.apache.org/download/)

[Install NSQ.io](http://nsq.io/deployment/installing.html)


## Setup cassandra keyspace

```
* cd vertice_gateway/db

* cqlsh -f base.cql

* cqlsh -f 1.5.cql

* cqlsh -f 1.5.1.cql

* cqlsh -f 1.5.2.cql

* cqlsh -f ee.cql

* cqlsh -f me.cql

```

## MEGAM_HOME

Create a home directory to store configuration files for MegamVertice

```

$ cd ~

$ mkdir -p megam/home/verticegateway

```

Edit your `.bashrc`

In your .bashrc file add the following line

```

export MEGAM_HOME=$HOME/megam/home

```

After this enter save the .bashrc file.Use the following command

  source ~/.bashrc  

## Configuration

Copy configuration files to $MEGAM_HOME/verticegateway

```

$ cd vertice_gateway (your cloned location)

$ cp conf/gateway.conf $MEGAM_HOME/verticegateway

$ cp conf/logger.xml $MEGAM_HOME/verticegateway


```

## Start Vertice Gateway

```
* cd vertice_gateway

* sbt

* clean

* compile

* run

```

![Gateway](https://github.com/megamsys/vertice_gateway/blob/1.5/public/images/vertice_gateway.png)

## Type the url `http://localhost:9000`

You'll see this in your browser.

```json
{
  "status" : {
    "casssandra" : "up",
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

# API Documentation

Refer [docs](https://docs.megam.io] and we'll publish vertice.raml shortly.

To generate the html docs.

```

npm install -g raml2html

raml2html vertice.raml

```


# Contribution

As this is heavy on memory, we have a work in progress 2.0 code which is based on rust.

# Documentation

For [documentation] (http://docs.megam.io)  [devkit] (https://github.com/megamsys/vertice_dev_kit)

# License

MIT


# Authors

Megam - Humans (<humans@megam.io>)
