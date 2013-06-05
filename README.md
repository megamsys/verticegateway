megam_play
==========

API server (REST based) for the "megam platform". The API server protectes the resources using [HMAC](http://www.ietf.org/rfc/rfc2104.txt) based authorization, which 
mean when a customer is onboarded an api_key is generated an stored in a secure datasource.. 

### Resources

|                      |            |                                             |
|:---------------------|:-----------|---------------------------------------------|
| **auth**       	   |    POST    | Verifies auth (for an email/api_key combo)  |
| 			     	   |            |                                             |
| **accounts**    	   |    GET     | List details of an account by email         |  
| **accounts\content** |	POST    | Onboard a new account using email/api_key   |
| 			     	   |            |                                             |
| **nodes**     	   |    GET     | List all the nodes list by node_id per email|
| **nodes\:node_id**   |    GET     | Show the detail of an node per email        |
| **nodes\content**    |    POST    | Onboard a new node per email                |
|		 		       |            |                                             |
| **logs**             |    GET     | Show all the logs per email                 |
| **logs\:node_id**    |    GET     | Show the log by node_id                     |

 
For future reading, 
* [megam_api](https://github.com/indykish/megam_api.git)
* [docs.megam.co](http://docs.megam.co)
* [slideshare - indykish](https://slideshare.net/indykish)


### Requirements

> 
[RabbitMQ 3.1.0 +](http://www.rabbitmq.com)
[OpenJDK 7.0](http://openjdk.java.net/install/index.html)
[Riak 1.3.1-1 +](http://docs.basho.com/riak/latest/downloads/)
[Erlang R15B01](http://www.erlang.org/)


### Tested on Ubuntu 13.04, AWS - EC2

## Usage

### Configuration

[Riak](http://basho.com) is used as the datastore to onboard a customer.


* Create a bucket in Riak named `megam-prov`

```
riak start

ps aux | grep riak

curl -v http://localhost:8098/riak/megam-prov

```

* Insert sanbox data  as below in riak. The key is `content` plus an unique id `1` generated automatically. 
  email:`sandy@megamsandbox.com`, api_key:`IamAtlas{74}NobodyCanSeeME#07`, authority: `user` 

```

curl -v -XPUT -d '{"id":"1","email":"sandy@megamsandbox.com", "api_key":"IamAtlas{74}NobodyCanSeeME#07","authority":"user"}' -H "Content-Type: application/json" http://localhost:8098/riak/accounts/content1         


curl http://localhost:8098/riak/accounts/content1

{"id":"1","email":"sandy@megamsandbox.com", "api_key":"IamAtlas{74}NobodyCanSeeME#07","authority":"user"}


```

* Update the conf\application.conf file with the `riak.url` 

```json

## Riak
## ~~~~~
riak.url="http://localhost:8690/megam-prov"


```
 
### Running megam_play (dev)

Before your run it,

* RabbitMQ Server is running

* Riak is running

###


### Testing (localhost)

* git clone https://github.com/indykish/megam_play.git

* sbt

* play run

Open another terminal

* sbt test

This is interfaced from megam_api ruby [megam_api](https://github.com/indykish/megam_api.git) 


### Production (`api.megam.co` in our case)

* Chef cookbooks used at megam [megam chef-repo](https://github.com/indykish/chef-repo)

* Run behind nginx server, load balanced.

* Riak datastore is clustered.

#### DEB Package using sbt.

The package structure shall be as per the debian guidelines. This uses sbt-native-packager plugin.

* `sbt clean compile stage`

* `sbt debian:package-bin`

Generates the .deb package for this project.

* `sbt debian:package-bin`

Generates the .deb file and runs the lintian command to look for issues in the package. 

* `sbt debian:package-lintian`


Once the megam_play_<v>.deb is built, its stored in S3 using [sbt-s3](https://github.com/sbt/sbt-s3)   

We are glad to help if you have questions, or request for new features..

[twitter](http://twitter.com/indykish) [email](<rajthilak@megam.co.in>)

#### TO - DO

* Interface to [megam_api - ruby](https://github.com/indykish/megam_api.git) , [Rail App](https://github.com/indykish/nilavu.git) and
  [CLI - Pug](https://github.com/indykish/meggy.git)

	
# License

|                      |                                          |
|:---------------------|:-----------------------------------------|
| **Author:**          | Rajthilak (<rajthilak@megam.co.in>)
|		       	       | KishorekumarNeelamegam (<nkishore@megam.co.in>)
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
 
