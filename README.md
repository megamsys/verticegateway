Megam Gateway
================

API server for "Megam PaaS". The API server protectes the RESTful resources using [HMAC](http://www.ietf.org/rfc/rfc2104.txt) based authorization, which 
mean when a customer is onboarded an api_key is generated an stored in a secure datasource.

[![Build Status](https://travis-ci.org/megamsys/megam_gateway.png)](https://travis-ci.org/megamsys/megam_gateway)


### Requirements

> 
[RabbitMQ 3.2.0 +](http://www.rabbitmq.com)
[OpenJDK 7.0](http://openjdk.java.net/install/index.html)
[Riak 2.0.2 +](http://docs.basho.com/riak/latest/downloads/)


### Setting up your development env.

```
* git clone https://github.com/megamsys/megam_gateway.git

* sbt

* clean 

* compile

* run

```

### Documentation

Refer [documentation] (http://www.gomegam.com/docs)




We are glad to help if you have questions, or request for new features..

[twitter](http://twitter.com/megamsys) [email](<support@megam.co.in>)

	
# License

|                      |                                          |
|:---------------------|:-----------------------------------------|
| **Author:**          | Rajthilak (<rajthilak@megam.co.in>)
|		       	       | KishorekumarNeelamegam (<nkishore@megam.co.in>)
|		       	       | Yeshwanth Kumar (<getyesh@megam.co.in>)
| **Copyright:**       | Copyright (c) 2012-2014 Megam Systems.
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
 
