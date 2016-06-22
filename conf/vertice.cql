//cqlsh 103.56.92.24 -f $MEGAM_HOME/verticegateway/vertice.cql

CREATE KEYSPACE IF NOT EXISTS vertice
  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }
 AND DURABLE_WRITES = false;

use vertice;

CREATE TABLE accounts( id text,  first_name text, last_name text,  phone text, email text,  api_key text, password text, authority text, password_reset_key text, password_reset_sent_at text, status text, created_at text, PRIMARY KEY (email));

CREATE TABLE assemblies (id text, org_id text, name text, assemblies list<text>, inputs list<text>, json_claz text, created_at text, PRIMARY KEY ((org_id), id));

CREATE TABLE assembly (id text, org_id text, account_id text, name text, components list<text>, tosca_type text, policies list<text>, inputs list<text>, outputs list<text>, status text, json_claz text, created_at text, PRIMARY KEY ((org_id), id));

CREATE TABLE components (id text, name text, tosca_type text, inputs list<text>, outputs list<text>, envs list<text>, artifacts text, related_components list<text>, operations list<text>, repo text, status text, json_claz text, created_at text, PRIMARY KEY (id));

CREATE TABLE settings(name text, scylla text, home text, api text, nsqd text, org text, domain text, route53_access_key text, route53_secret_key text, docker_swarm text, one_endpoint text, one_userid text, one_password text, primary key(name));

CREATE TABLE organizations(id text, accounts_id text, name text, json_claz text, created_at  text, PRIMARY KEY ((accounts_id), id)  );

CREATE TABLE domains (id text, org_id text,  name text, json_claz text, created_at text, PRIMARY KEY((org_id), name));

CREATE TABLE sshkeys (id text, org_id text, name text, privatekey text, publickey text, json_claz text, created_at text, PRIMARY KEY((org_id), name));

CREATE TABLE requests( id text, cat_id text, cattype text,  name text,  action text, category text, created_at text, PRIMARY KEY (id));

CREATE TABLE marketplaces ( settings_name text,  cattype text, flavor text,  image text, catorder text, url text, json_claz text, envs list<text>,  options list<text>, plans map <text, text>,  PRIMARY KEY ((settings_name), flavor) );

CREATE TABLE billedhistories (id text, account_id text, assembly_id text, bill_type text, billing_amount text, currency_type text, json_claz text, created_at text, PRIMARY KEY((account_id, assembly_id), bill_type, created_at));

CREATE TABLE billingtranscations (id text, account_id text, gateway text, amountin text, amountout text, fees text, tranid text, trandate text, currency_type text,  created_at text, json_claz text, PRIMARY KEY(account_id, trandate, created_at));

CREATE TABLE balances (id text, account_id text, credit text, json_claz text, created_at text, updated_at text, PRIMARY KEY((account_id)));

CREATE TABLE sensors (id text,  account_id text, sensor_type text, assembly_id text, assembly_name text, assemblies_id text, node text, system text, status text, source text, message text, audit_period_beginning text, audit_period_ending text, audit_period_delta text, metrics list<text>, created_at text, PRIMARY KEY((account_id, assembly_id), sensor_type));

CREATE TABLE events (event_type text,  account_id text, assembly_id text, data list<text>, created_at text, PRIMARY KEY((account_id), assembly_id, event_type,  created_at));

CREATE TABLE addons (id text, provider_id text, account_id text, provider_name text, options list<text>, created_at text, PRIMARY KEY ((account_id), provider_name);

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, options, plans) values ('vertice', 'TORPEDO', 'Ubuntu', 'ubuntu.png', '1', 'http://ubuntu.com', 'Megam::MarketPlace' , [], [], {'14.04':'Ubuntu 14.04 LTS (Trusty) is delivers the best value scale-out performance available.'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, options, plans) values ('vertice', 'TORPEDO', 'CoreOs', 'coreos.png', '1', 'http://coreos.com', 'Megam::MarketPlace' , [], [], {'835.13.0': 'CoreOS provides no package manager as a way for the distribution of applications, requiring instead all applications to run inside their containers'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, options, plans) values ('vertice', 'TORPEDO', 'Debian', 'debian.png', '1', 'http://debian.com', 'Megam::MarketPlace' , [], [], {'8.0':'Debian is a free operating system (OS) for your computer. Jessie is the codename for 8.0 Debian.'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, options, plans) values ('vertice', 'TORPEDO', 'CentOs', 'centos.png', '1', 'http://debian.com', 'Megam::MarketPlace' , [], [], {'7.1':'CentOS is an Enterprise-class Linux Distribution derived from sources freely provided to the public by Red Hat.'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, options, plans) values ('vertice', 'APP', 'Java', 'java.png', '2', 'https://github.com/megamsys/java-spring-petclinic.git', 'Megam::MarketPlace' , ['{"key":"port", "value":"8080"}', '{"key":"tomcat_username", "value":"megam"}', '{"key":"tomcat_password", "value":"megam"}'], [], {'8.x':'Quickly get started with J2EE Spring framework app.'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, options, plans) values ('vertice', 'APP', 'Rails', 'rails.png', '2', 'https://github.com/megamsys/aryabhata.git', 'Megam::MarketPlace' , [], [], {'4.x':'Quickly get started with rails 4.x app.'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, options, plans) values ('vertice', 'APP', 'Play', 'play.png', '2', 'https://github.com/megamsys/modern-web-template.git', 'Megam::MarketPlace' , ['{"key":"port", "value":"9000"}'], [], {'2.4.x':'Build robust RESTful API server using Scala.'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, options, plans) values ('vertice', 'APP', 'Nodejs', 'nodejs.png', '2', 'https://github.com/megamsys/etherpad-lite.git', 'Megam::MarketPlace' , [], [], {'0.12.x':'Build fast, scalable, and incredibly efficient realtime app in second.'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, options, plans) values ('vertice', 'APP', 'PHP', 'php.png', '2', 'https://secure.php.net/', 'Megam::MarketPlace' , ['{"key":"port", "value":"8080"}'], [], {'5.x':'PHP is a server-side scripting language designed for web development but also used as a general-purpose programming language.'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, options, plans) values ('vertice', 'APP', 'CCPlus', 'ccplus.png', '2', 'https://www.bell-labs.com/', 'Megam::MarketPlace' , ['{"key":"port", "value":"80"}'], [], {'4.9': 'C/C++ IDE for working with C/C++ programming.'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, options, plans) values ('vertice', 'APP', 'Python', 'python.png', '2', 'https://www.python.org/', 'Megam::MarketPlace' , ['{"key":"port", "value":"80"}'], [], {'3.4.2':'Python is object-oriented, imperative and functional programming language.'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, options, plans) values ('vertice', 'SERVICE', 'Apache', 'apache.png', '3', 'http://www.apache.org/', 'Megam::MarketPlace' , ['{"key":"port", "value":"80"}'], [], {'2.4.18':'Apache Server is an open-source HTTP server for modern operating systems'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, options, plans) values ('vertice', 'SERVICE', 'CouchDb', 'couchdb.png', '3', 'http://couchdb.apache.org/', 'Megam::MarketPlace' , ['{"key":"port", "value":"5984"}', '{"key":"username", "value": "couchdb"}'], [], {'1.6.1':'CouchDB is a powerful, open source document-oriented database system.'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, options, plans) values ('vertice', 'SERVICE', 'PostgreSQL', 'postgres.png', '3', 'https://postgresql.org', 'Megam::MarketPlace' , ['{"key":"port","value":"5432"}', '{"key":"username", "value":"postgres"}', '{"key":"password", "value":"megam"}'], [], {'9.3':'PostgreSQL is a powerful, open source object-relational database system.'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, options, plans) values ('vertice', 'SERVICE', 'Riak', 'riak.png', '3', 'http://s3.amazonaws.com/downloads.basho.com/riak/2.1/2.1.1/ubuntu/trusty/riak_2.1.1-1_amd64.deb', 'Megam::MarketPlace' , ['{"key":"port", "value":"8098"}'], [], {'2.1.1.':'PostgreSQL is a powerful, open source object-relational database system.'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, options, plans) values ('vertice', 'SERVICE', 'Redis', 'redis.png', '3', 'https://redis.org', 'Megam::MarketPlace' , ['{"key":"port", "value":"6379"}'], [], {'2.8.4':'Redis is a key-value store which acts as a data structure server with keys containing strings, hashes, lists, sets and sorted sets.'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, options, plans) values ('vertice', 'SERVICE', 'RabbitMQ', 'rabbitmq.png', '3', 'https://www.rabbitmq.com', 'Megam::MarketPlace' , ['{"key":"port", "value":"5672"}', '{"key":"username", "value":"guest"}', '{"key":"password", "value":"guest"}'], [], {'3.3.x':'RabbitMQ is a message broker software  that implements the Advanced Message Queuing Protocol (AMQP).'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, options, plans) values ('vertice', 'SERVICE', 'Mysql', 'mysql.png', '3', 'https://dev.mysql.com/', 'Megam::MarketPlace' , ['{"key":"port", "value":"3306"}', '{"key":"username", "value":"root"}', '{"key":"password", "value":"megam"}'], [], {'5.x':'MySQL is a freely available open source Relational Database Management System (RDBMS) that uses Structured Query Language (SQL).'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, options, plans) values ('vertice', 'MICROSERVICES', 'DockerBox', 'dockerbox.png', '4', 'https://www.docker.com', 'Megam::MarketPlace' , ['{"key":"port", "value":"2375"}'], [], {'0.9.0':'Launch a vm with docker in detached model.'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, options, plans) values ('vertice', 'MICROSERVICES', 'DockerContainer',  'dockercontainer.png', '4', 'https://www.docker.com', 'Megam::MarketPlace' , [], [], {'1.8.x':'Docker that automates the deployment of applications inside software containers.'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, options, plans) values ('vertice', 'ANALYTICS', 'Hadoop',  'hadoop.png', '5', 'https://hadoop.apache.org', 'Megam::MarketPlace' , ['{"key":"port", "value":"50070"}', '{"key":"oneclick", "value":"yes"}'], [], {'2.6.0':'Apache Hadoop is a set of algorithms (an open-source software framework) for distributed storage and distributed processing of very large data sets (Big Data) on computer clusters built from commodity hardware.'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, options, plans) values ('vertice', 'ANALYTICS', 'Spark', 'spark.png', '5', 'http://spark.apache.org/', 'Megam::MarketPlace' , ['{"key":"port", "value":"9004"}', '{"key":"oneclick", "value":"yes"}'], [], {'1.5.0':'Apache Spark is a fast and general-purpose cluster computing system. It provides high-level APIs in Java, Scala, Python and R, and an optimized engine that supports general execution graphs.'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, options, plans) values ('vertice', 'ANALYTICS', 'SparkNotebook', 'sparknotebook.png', '5', 'http://spark-notebook.io', 'Megam::MarketPlace' , ['{"key":"port", "value":"9000"}', '{"key":"oneclick", "value":"yes"}'], [], {'0.6.x':'Use Apache Spark straight from the Browser. '});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, options, plans) values ('vertice', 'COLLABORATION', 'Zarafa', 'zarafa.png', '6', 'https://www.zarafa.com/', 'Megam::MarketPlace' , ['{"key":"port", "value":"9000"}','{"key":"oneclick", "value":"yes"}'], [], {'7.2':'Zarafa gives you the tools that help boost your day-to-day communication. File sharing, web meetings and business apps like email and calendar are integrated in one suite.'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, options, plans) values ('vertice', 'COLLABORATION', 'Owncloud', 'owncloud.png', '6', 'https://owncloud.org/', 'Megam::MarketPlace' , ['{"key":"username", "value":"admin"}', '{"key":"password", "value":"admin"}'], [], {'8.1':'Access your data from all your devices, on an open platform you can extend and modify.'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, options, plans) values ('vertice', 'COLLABORATION', ' VirtualOffice',  'virtual_office.png', '6', 'www.megam.io', 'Megam::MarketPlace' , ['{"key":"host", "value":""}', '{"key":"port", "value":"806"}'], ['{"key":"oneclick", "value":"true"}'], {'0.9.0':'A virtual office provides bundled applications for everyday office use.'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'BITNAMI', 'WordPress ',  'wordpress.png', '7', 'https://bitnami.com/stack/wordpress', 'Megam::MarketPlace' , ['{"key":"bitnami_url", "value":"https://s3-ap-southeast-1.amazonaws.com/megampub/bitnami/Blog/bitnami-wordpress-4.4.2-3-linux-x64-installer.run"}'], {'4.4.2':'WordPress is the web publishing platforms for building blogs and websites.'});


insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'BITNAMI', 'Moodle',  'moodle.png', '7', 'https://bitnami.com/stack/moodle', 'Megam::MarketPlace' , ['{"key":"bitnami_url", "value":"https://s3-ap-southeast-1.amazonaws.com/megampub/bitnami/eLearning/bitnami-moodle-3.0.3-0-linux-x64-installer.run"}'], {'3.0.3':'Moodle is a very popular open source learning management solution (LMS) for the delivery of elearning courses and programs.'});


insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'BITNAMI', ' PrestaShop',  'prestashop.png', '7', 'https://bitnami.com/stack/prestashop', 'Megam::MarketPlace' , ['{"key":"bitnami_url", "value":"https://s3-ap-southeast-1.amazonaws.com/megampub/bitnami/e-Commerce/bitnami-prestashop-1.6.1.4-0-linux-x64-installer.run"}'], {'1.6.1.4':'PrestaShop has perhaps the most unique and interesting out of the box experience of any ecommerce platform '});\


insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'BITNAMI', 'GitLab',  'gitlab.png', '7', 'https://bitnami.com/stack/gitlab', 'Megam::MarketPlace' , ['{"key":"bitnami_url", "value":"https://s3-ap-southeast-1.amazonaws.com/megampub/bitnami/Version+Control/bitnami-gitlab-8.5.7-0-linux-x64-installer.run"}'], {'8.5.7':'The GitLab open source edition is a developer tool that allows users to collaborate on code, create new projects, manage repositories, and perform code reviews. '});


insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'BITNAMI', 'MediaWiki',  'mediawiki.png', '7', 'https://bitnami.com/stack/mediawiki', 'Megam::MarketPlace' , ['{"key":"bitnami_url", "value":"https://s3-ap-southeast-1.amazonaws.com/megampub/bitnami/Wiki/bitnami-mediawiki-1.26.2-1-linux-x64-installer.run"}'], {'1.26.2':'MediaWiki is the engine behind Wikipedia, the behemoth website with millions of articles written by millions of users and translated into dozens of languages.'});


insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'BITNAMI', 'Mautic',  'mautic.png', '7', 'https://bitnami.com/stack/mautic', 'Megam::MarketPlace' , ['{"key":"bitnami_url", "value":"https://s3-ap-southeast-1.amazonaws.com/megampub/bitnami/Marketing+Automation/bitnami-mautic-1.3.1-0-linux-x64-installer.run"}'], {'1.3.1':'Mautic is marketing automation software for everyone.Mautic is free and open source, and can be easily integrated with third-party applications through a REST API and web-hooks.'});


insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'BITNAMI', 'ProcessMaker Open Source Edition',  'processmaker.png', '7', 'https://bitnami.com/stack/processmaker', 'Megam::MarketPlace' , ['{"key":"bitnami_url", "value":"https://s3-ap-southeast-1.amazonaws.com/megampub/bitnami/BPM/bitnami-processmaker-3.0.1.7-0-linux-x64-installer.run"}'], {'3.0.1.7':'ProcessMaker is an open source Business Process Management (BPM) platform designed to automate workflows and improve business efficiency'});


insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'BITNAMI', 'DreamFactory',  'dreamfactory.png', '7', 'https://bitnami.com/stack/dreamfactory', 'Megam::MarketPlace' , ['{"key":"bitnami_url", "value":"https://s3-ap-southeast-1.amazonaws.com/megampub/bitnami/Mobile+Development/bitnami-dreamfactory-2.1.1-2-linux-x64-installer.run"}'], {'2.1.1':'DreamFactory is an open source REST API platform for mobile, web, and IoT applications.'});


insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'BITNAMI', 'Pimcore',  'pimcore.png', '7', 'https://bitnami.com/stack/pimcore', 'Megam::MarketPlace' , ['{"key":"bitnami_url", "value":"https://s3-ap-southeast-1.amazonaws.com/megampub/bitnami/CMS/bitnami-pimcore-3.1.1-2-linux-x64-installer.run"}'], {'3.1.1':'Pimcore is a powerful multi-channel experience and engagement management (CEM/CXM) platform.'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'BITNAMI', 'Ghost',  'ghost.png', '7', 'https://bitnami.com/stack/ghost', 'Megam::MarketPlace' , ['{"key":"bitnami_url", "value":"https://s3-ap-southeast-1.amazonaws.com/megampub/bitnami/Blog/bitnami-ghost-0.7.8-0-linux-x64-installer.run"}'], {'0.7.8':'Ghost is built on Node.js and is extremely easy to configure and customize.'});


insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'BITNAMI', 'Redmine',  'redmine.png', '7', 'https://bitnami.com/stack/redmine', 'Megam::MarketPlace' , ['{"key":"bitnami_url", "value":"https://s3-ap-southeast-1.amazonaws.com/megampub/bitnami/Bug+Tracking/bitnami-redmine-3.2.1-0-linux-x64-installer.run"}'], {'3.2.1':'Redmine is a very popular Rails-based open source bug tracker to help you manage issues and tasks for multiple projects.'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'BITNAMI', 'Jenkins',  'jenkins.png', '7', 'https://bitnami.com/stack/jenkins', 'Megam::MarketPlace' , ['{"key":"bitnami_url", "value":"https://s3-ap-southeast-1.amazonaws.com/megampub/bitnami/Continuous+Integration/bitnami-jenkins-1.655-0-linux-x64-installer.run"}'], {'1.655':'It’s widely recognized as the most feature-rich CI available with easy configuration, continuous delivery and continuous integration support, easily test, build and stage your app, and more.'});

insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'BITNAMI', 'Discourse',  'discourse.png', '7', 'https://bitnami.com/stack/discourse', 'Megam::MarketPlace' , ['{"key":"bitnami_url", "value":"https://s3-ap-southeast-1.amazonaws.com/megampub/bitnami/Forum/bitnami-discourse-1.4.6-0-linux-x64-installer.run"}'], {'1.4.6':'Discourse is the next-next-generation community forum platform. Discourse has a thoroughly modern design and is written in JavaScript.'});


insert into marketplaces (settings_name, cattype, flavor, image, catorder, url, json_claz, envs, plans) values ('vertice', 'BITNAMI', 'eXo Platform',  'exoplatform.png', '7', 'https://bitnami.com/stack/exo-platform-community', 'Megam::MarketPlace' , ['{"key":"bitnami_url", "value":"https://s3-ap-southeast-1.amazonaws.com/megampub/bitnami/Social+Networking/bitnami-exoplatform-4.3.0-1-linux-x64-installer.run"}'], {'4.3.0':'eXo Platform combines collaboration, calendering, content and document management and other productivity tools with social features to provide a complete enterprise intranet.'});
