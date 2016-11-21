VirtEngine Gateway
================

API server for "[VirtEngine](https://virtengine.com)".

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

* git clone https://github.com/<your_github_id>/gateway.git

* cd gateway

* sbt

* clean

* compile


```

## Running

[Install Cassandra](http://cassandra.apache.org/download/)

[Install NSQ.io](http://nsq.io/deployment/installing.html)


## Setup cassandra keyspace

```
* cd gateway/db

* cqlsh -f base.cql

* cqlsh -f 1.5.cql

* cqlsh -f ee.cql

* cqlsh -f me.cql


```

## MEGAM_HOME

Create a home directory to store configuration files for MegamVertice

```

$ cd ~

$ mkdir -p detio/virtenginegateway

```

Edit your `.bashrc`

In your .bashrc file add the following line

```

export MEGAM_HOME=$HOME/detio

```

After this enter save the .bashrc file.Use the following command

  source ~/.bashrc  

## Configuration

Copy configuration files to $MEGAM_HOME/virtenginegateway

```

$ cd vertice_gateway (your cloned location)

$ cp conf/gateway.conf $MEGAM_HOME/virtenginegateway

$ cp conf/logger.xml $MEGAM_HOME/virtenginegateway


```

## Start Vertice Gateway

```
* cd gateway

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

Update the vertice.raml

```

npm install -g raml2html

raml2html vertice.raml

```


# Contribution

As this is heavy on memory, we have a work in progress 2.0 code which is based on rust.

# Documentation

For [documentation] (http://docs.virtengine.com)  [devkit] (https://github.com/megamsys/vertice_dev_kit)

# License

MIT


# Authors

VirtEngine Humans <hello@virtengine.com>
