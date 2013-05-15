megam_play
==========

API server (REST based) for the "megam platform". The API server allows the following resources. 

### Resources

|                      |            |
|:---------------------|:-----------|
| **auth**       	   |    GET
| 			     	   | 
| **accounts**    	   |    GET
| 			     	   | 
| **nodes**     	   |    GET
| **nodes\:node_id**   |    GET
| **nodes\content**    |    POST
|		 		       | 
| **logs**             |    GET
| **logs\:node_id**    |    GET

The resources are portected using [HMAC](http://www.ietf.org/rfc/rfc2104.txt) based authorization, which 
mean when a customer is onboarded a shared key is generated in stored in a datasource. [Riak](http:\\basho.com) is 
used as the datastore to onboard a customer.
 
For future reading, 
* [megam_api](https:\\github.com\indykish\megam_api.git)
* [docs.megam.co](http:\\docs.megam.co)
* [slideshare - indykish](https:\\slideshare.net\indykish)


### Requirements

> 
[RabbitMQ 3.1.0 +](http://www.rabbitmq.com)
[OpenJDK 7.0](http://openjdk.java.net/install/index.html)
[Riak 1.3.1-1 +](http://docs.basho.com/riak/latest/downloads/)
[Erlang R15B01](http://www.erlang.org/)


#### Tested on Ubuntu 13.04, AWS - EC2

## Usage

At the minimum you need a riak bucket changed in the conf\application.conf file.

```json

# Riak
# ~~~~~
riak.url="http://localhost:8690/bucket"


```


### Running megam_play

Before your run it,

* RabbitMQ Server is running


* Riak is running

### Testing (localhost)

> git clone https://github.com/indykish/megam_play.git

> sbt

> play run

Open another terminal

> sbt test

This is interfaced from megam_api ruby [megam_api](https://github.com/indykish/megam_api.git) 


### Production (`api.megam.co` in our case)

* Chef cookbooks used at megam * coming soon. [megamn chef-repo](https://github.com/indykish/chef-repo)

> Run behind nginx server, load balanced.

> Riak datastore is clustered.
   


We are glad to help if you have questions, or request for new features..

[twitter](http://twitter.com/indykish) [email](<rajthilak@megam.co.in>)

#### TO - DO

* Cookbook for megam_play which runs behind - in progress [nginx](http://nginx.org)

* Interface to megam_common to utilize RabbitMQ

	
# License

|                      |                                          |
|:---------------------|:-----------------------------------------|
| **Author:**          | Rajthilak (<rajthilak@megam.co.in>)
|		       | KishorekumarNeelamegam (<nkishore@megam.co.in>)
| **Copyright:**       | Copyright (c) 2012-2013 Megam Systems.
| **License:**         | Apache License, Version 2.0

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 
