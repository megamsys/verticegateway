-- Schema for enterprise edition

use vertice;

-- Nuke stuff that is there in marketplaces

DELETE FROM marketplaces WHERE flavor in
('Ubuntu','Debian','CoreOS','CentOS','Fedora','Windows','DockerMachine','FreeBSD',
 'Apache', 'CouchDB', 'PostgreSQL', 'MySQL', 'Riak', 'Redis', 'RabbitMQ', 'Zarafa', 'OwnCloud', 'WordPress', 'Moodle', 'PrestaShop',
 'Ghost','Discourse','Redmine','VestaCP','Ajenti',
 'Java','Rails','PHP', 'Play','Nodejs','CCPlus','Python',
 'DockerContainer', 'Hadoop', 'Spark', 'SparkNotebook',
 'DreamFactory','Pimcore','Jenkins','eXo Platform', 'Jenkins', 'Mautic', 'ProcessMaker Open Source Edition');

-- Machines
-- Ubuntu, Fedora, CentOS,  CoreOS, Windows, DockerMachine

INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
    ('MKT000001','Ubuntu', 'vertice', 'TORPEDO', '1',
     'active', 'ubuntu.png', 'http://www.ubuntu.com/server', [],[],[],[],[],
     ['{"key":"14.04","value":"Ubuntu is a Debian-based Linux operating system. Trusty Tahr is the Ubuntu codename for version 14.04 LTS of the Ubuntu Linux-based operating system."}',
      '{"key":"16.04", "value":"Ubuntu is a Debian-based Linux operating system. Xenial Xerus is the Ubuntu codename for version 16.04 LTS of the Ubuntu Linux-based operating system."}'],
     'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));

INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
    ('MKT000002','Debian', 'vertice', 'TORPEDO', '1',
     'active', 'debian.png', 'https://www.debian.org/releases/stable/', [],[],[],[],[],
     ['{"key":"8.5","value":"Debian is a Unix-like computer operating system that is composed entirely of free software, most of which is under the GNU General Public License, and packaged by a group of individuals called the Debian Project."}'],
     'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));

INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options,  inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
    ('MKT000003','CoreOS', 'vertice', 'TORPEDO', '1',
     'active', 'coreos.png', 'https://coreos.com/why/', [],[],[],[],[],
     ['{"key":"latest", "value": "CoreOS is a lightweight Linux operating system designed for clustered deployments providing automation, security, and scalability for your most critical applications."}'],
     'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));

INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options,  inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
    ('MKT000004','CentOS', 'vertice', 'TORPEDO', '1',
     'active', 'centos.png', 'https://www.centos.org', [],[],[],[],[],
     ['{"key":"7.2", "value": "CentOS is a Linux distribution that attempts to provide a free, enterprise-class, community-supported computing platform which aims to be functionally compatible with its upstream source, Red Hat Enterprise Linux (RHEL)."}',
     '{"key" :"6.8", "value": "CentOS is a Linux distribution that attempts to provide a free, enterprise-class, community-supported computing platform which aims to be functionally compatible with its upstream source, Red Hat Enterprise Linux (RHEL)."}'],
    'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));

INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
     ('MKT000005','Fedora', 'vertice', 'TORPEDO', '1',
      'active', 'fedora.png', 'https://getfedora.org/en/server/', [], [],[],[],[],
      ['{"key":"24", "value": "Fedora /fᵻˈdɒr.ə/ (formerly Fedora Core) is an operating system based on the Linux kernel, developed by the community-supported Fedora Project and sponsored by Red Hat."}'],
       'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));


INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
    ('MKT000006', 'Windows', 'vertice', 'TORPEDO', '1',
     'active', 'windows.png', 'https://www.microsoft.com/en-us/cloud-platform/windows-server-2012-r2', [], [],[],[],[],
     ['{"key":"2008_R2", "value": "Windows Server 2008 R2 STANDARD EVALUATION 180 days builds on the award-winning foundation of Windows Server 2008, expanding existing technology and adding new features to enable organizations to increase the reliability and flexibility of their server infrastructures."}',
      '{"key":"2012_R2", "value": "Windows Server 2012 R2 STANDARD EVALUATION 180 days brings our experience delivering global-scale cloud services into your infrastructure with features and enhancements in virtualization, management, storage, networking, virtual desktop infrastructure, access and information protection, and the web and application platform."}'],
    'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));


INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
     ('MKT000007','DockerMachine', 'vertice', 'TORPEDO', '1',
     'active', 'dockermachine.png', 'https://docs.docker.com/machine/overview', ['{"key":"port.tcp", "value":"2376"}'], [], [],[],[],
     ['{"key":"1.12", "value": "Install Docker Engine on virtual hosts, and manage the hosts with docker-machine"}'],
    'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));


INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
    ('MKT000008','FreeBSD', 'vertice', 'TORPEDO', '1',
     'active', 'freebsd.png', 'https://www.freebsd.org/', [],[],[],[],[],
     ['{"key":"10.3", "value": "FreeBSD is an advanced computer operating system used to power modern servers, desktops, and embedded platforms."}'],
    'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));


-- Common collaboration software
-- Apache2, PostgreSQL, MySQL, Cassandra, Riak, RabbitMQ, Redis

INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
    ('MKT000009', 'Apache', 'vertice', 'SERVICE', '3',
     'active', 'apache.png', 'https://www.apache.org', ['{"key":"port.http", "value":"80"}'],[],[],[],[],
     ['{"key":"2.4.18", "value": "Apache is the most widely used web server software. Developed and maintained by Apache Software Foundation."}'],
    'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));


INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
    ('MKT000010', 'CouchDB', 'vertice', 'SERVICE', '3',
     'active', 'couchdb.png', 'https://couchdb.apache.org', ['{"key":"port.http", "value":"5984"}', '{"key":"username", "value": "couchdb"}'],[],[],[],[],
     ['{"key":"1.6.1", "value": "CouchDB is a database that completely embraces the web. Store your data with JSON documents. Access your documents and query your indexes with your web browser, via HTTP. Index, combine, and transform your documents with JavaScript."}'],
    'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));


INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
    ('MKT000011', 'PostgreSQL', 'vertice', 'SERVICE', '3',
     'active', 'postgres.png', 'https://postgresql.org', ['{"key":"port.tcp", "value":"5432"}', '{"key":"username", "value": "postgres"}',  '{"key":"password", "value":"megam"}'],[],[],[],[],
     ['{"key":"9.5", "value": "PostgreSQL is an open source relational database management system ( DBMS ) developed by a worldwide team of volunteers."}'],
    'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));


INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
    ('MKT000012', 'Riak', 'vertice', 'SERVICE', '3',
     'active', 'riak.png', 'http://docs.basho.com', ['{"key":"port.http", "value":"8098"}'],[],[],[],[],
     ['{"key":"2.1.4", "value": "Riak KV is a distributed NoSQL database that is highly available, scalable and easy to operate. It automatically distributes data across the cluster to ensure fast performance and fault-tolerance."}'],
     'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));


INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
     ('MKT000013', 'Redis', 'vertice', 'SERVICE', '3',
      'active', 'redis.png', 'https://redis.io', ['{"key":"port.tcp", "value":"6937"}'],[],[],[],[],
      ['{"key":"2.8.4", "value": "Redis is an open source (BSD licensed), in-memory data structure store, used as database, cache and message broker."}'],
      'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));


INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
       ('MKT000014', 'RabbitMQ', 'vertice', 'SERVICE', '3',
        'active', 'rabbitmq.png', 'https://www.rabbitmq.com', ['{"key":"port.http", "value":"15672"}', '{"key":"username", "value":"guest"}', '{"key":"password", "value":"guest"}'],[],[],[],[],
        ['{"key":"3.5.x", "value": "RabbitMQ is open source message broker software (sometimes called message-oriented middleware) that implements the Advanced Message Queuing Protocol (AMQP)."}'],
        'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));

INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
        ('MKT000015', 'MySQL', 'vertice', 'SERVICE', '3',
        'active', 'mysql.png', 'https://dev.mysql.com/', ['{"key":"port.tcp", "value":"3306"}', '{"key":"username", "value":"root"}', '{"key":"password", "value":"megam"}'],[],[],[],[],
        ['{"key":"5.x", "value": "MySQL, the most popular Open Source SQL database management system, is developed, distributed, and supported by Oracle Corporation."}'],
        'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));

-- Zarafa, OwnCloud, WordPress, Moodle, PrestaShop,MediaWiki
-- Ghost, Redmine, Discourse, Agenti, VestaCP, ISPConfig, Webmin, Froxlor

INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
        ('MKT000016', 'Zarafa', 'vertice', 'COLLABORATION', '6',
        'active', 'zarafa.png', 'https://www.zarafa.com/', ['{"key":"port.http", "value":"80"}', '{"key":"url.suffix", "value":"/webaccess"}'], ['{"key":"oneclick", "value":"yes"}'],[],[],[],
        ['{"key":"7.2", "value": "Zarafa gives you the tools that help boost your day-to-day communication. File sharing, web meetings and business apps like email and calendar are integrated in one suite."}'],
        'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));


INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
      ('MKT000017', 'OwnCloud', 'vertice', 'COLLABORATION', '6',
      'active', 'owncloud.png', 'https://owncloud.org/', ['{"key":"port.http", "value":"80"}', '{"key":"url.suffix", "value":"/"}'],
      ['{"key":"oneclick", "value":"yes"}','{"key":"bitnami_username", "value":"team4megam"}',
      '{"key":"bitnami_password", "value":"team4megam"}', '{"key":"bitnami_owncloud_site", "value":"127.0.0.1"}',
      '{"key":"bitnami_url", "value":"https://s3-ap-southeast-1.amazonaws.com/megampub/bitnami/MediaSharing/bitnami-owncloud-9.1.3-1-linux-x64-installer.run"}'],
      [],[],[],
      ['{"key":"9.1", "value": "Access your data from all your devices, on an open platform you can extend and modify."}'],
      'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));


INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
      ('MKT000018', 'WordPress', 'bitnami', 'COLLABORATION', '6',
      'active', 'wordpress.png', 'https://bitnami.com/stack/wordpress', ['{"key":"port.http", "value":"80"}', '{"key":"url.suffix", "value":"/"}'],
      ['{"key":"oneclick", "value":"yes"}','{"key":"bitnami_username", "value":"team4megam"}', '{"key":"bitnami_password", "value":"team4megam"}',
      '{"key":"bitnami_url", "value":"https://s3-ap-southeast-1.amazonaws.com/megampub/bitnami/Blog/bitnami-wordpress-4.4.2-3-linux-x64-installer.run"}'],
      [],[],[],
      ['{"key":"4.4.2", "value": "WordPress is the web publishing platforms for building blogs and websites."}'],
      'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));

INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
      ('MKT000019', 'Moodle', 'bitnami', 'COLLABORATION', '6',
      'active', 'moodle.png', 'https://bitnami.com/stack/moodle', ['{"key":"port.http", "value":"80"}', '{"key":"url.suffix", "value":"/"}'],
       ['{"key":"oneclick", "value":"yes"}','{"key":"bitnami_username", "value":"team4megam"}', '{"key":"bitnami_password", "value":"team4megam"}',
       '{"key":"bitnami_url", "value":"https://s3-ap-southeast-1.amazonaws.com/megampub/bitnami/eLearning/bitnami-moodle-3.0.3-0-linux-x64-installer.run"}'],
        [],[],[],
        ['{"key":"3.0.3", "value": "Moodle is a very popular open source learning management solution (LMS) for the delivery of elearning courses and programs."}'],
        'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));

INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
      ('MKT000020', 'PrestaShop', 'bitnami', 'COLLABORATION', '6',
      'active', 'prestashop.png', 'https://bitnami.com/stack/prestashop', ['{"key":"port.http", "value":"80"}', '{"key":"url.suffix", "value":"/"}'],
      ['{"key":"oneclick", "value":"yes"}','{"key":"bitnami_username", "value":"team4megam"}',
       '{"key":"bitnami_password", "value":"team4megam"}', '{"key":"bitnami_prestashop_site", "value":"127.0.0.1"}',
       '{"key":"bitnami_url", "value":"https://s3-ap-southeast-1.amazonaws.com/megampub/bitnami/e-Commerce/bitnami-prestashop-1.6.1.4-0-linux-x64-installer.run"}'],
        [],[],[],
        ['{"key":"1.6.1.4", "value": "PrestaShop has perhaps the most unique and interesting out of the box experience of any ecommerce platform "}'],
      'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));


INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
      ('MKT000021', 'MediaWiki', 'bitnami', 'COLLABORATION', '6',
      'active', 'mediawiki.png', 'https://bitnami.com/stack/mediawiki', ['{"key":"port.http", "value":"80"}', '{"key":"url.suffix", "value":"/"}'],
      ['{"key":"oneclick", "value":"yes"}','{"key":"bitnami_username", "value":"team4megam"}', '{"key":"bitnami_password", "value":"team4megam"}',
       '{"key":"bitnami_url", "value":"https://s3-ap-southeast-1.amazonaws.com/megampub/bitnami/Wiki/bitnami-mediawiki-1.26.2-1-linux-x64-installer.run"}'],
        [],[],[],
        ['{"key":"1.26.2", "value": "MediaWiki is the engine behind Wikipedia, the behemoth website with millions of articles written by millions of users and translated into dozens of languages."}'],
      'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));


INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
    ('MKT000022', 'Ghost', 'bitnami', 'COLLABORATION', '6',
    'active', 'ghost.png', 'https://bitnami.com/stack/ghost', ['{"key":"port.http", "value":"80"}', '{"key":"url.suffix", "value":"/"}'],
    ['{"key":"oneclick", "value":"yes"}','{"key":"bitnami_username", "value":"team4megam"}', '{"key":"bitnami_password", "value":"team4megam"}',
   '{"key":"bitnami_url", "value":"https://s3-ap-southeast-1.amazonaws.com/megampub/bitnami/Blog/bitnami-ghost-0.7.8-0-linux-x64-installer.run"}'],
    [],[],[],
    ['{"key":"0.7.8", "value": "Ghost is built on Node.js and is extremely easy to configure and customize."}'],
    'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));


INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
    ('MKT000023', 'Redmine', 'bitnami', 'COLLABORATION', '6',
    'active', 'redmine.png', 'https://bitnami.com/stack/redmine', ['{"key":"port.http", "value":"80"}', '{"key":"url.suffix", "value":"/"}'],
    ['{"key":"oneclick", "value":"yes"}','{"key":"bitnami_username", "value":"team4megam"}', '{"key":"bitnami_password", "value":"team4megam"}',
    '{"key":"bitnami_url", "value":"https://s3-ap-southeast-1.amazonaws.com/megampub/bitnami/Bug+Tracking/bitnami-redmine-3.2.1-0-linux-x64-installer.run"}'],
    [],[],[],
    ['{"key":"3.2.1", "value": "Redmine is a very popular Rails-based open source bug tracker to help you manage issues and tasks for multiple projects."}'],
   'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));

INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
   ('MKT000024', 'Discourse', 'bitnami', 'COLLABORATION', '6',
   'active', 'discourse.png', 'https://bitnami.com/stack/discourse', ['{"key":"port.http", "value":"80"}', '{"key":"url.suffix", "value":"/"}'],
   ['{"key":"oneclick", "value":"yes"}','{"key":"bitnami_username", "value":"team4megam"}', '{"key":"bitnami_password", "value":"team4megam"}',
   '{"key":"bitnami_url", "value": "https://s3-ap-southeast-1.amazonaws.com/megampub/bitnami/Forum/bitnami-discourse-1.4.6-0-linux-x64-installer.run"}'],
   [],[],[],
   ['{"key":"1.4.6", "value": "Discourse is the next-next-generation community forum platform. Discourse has a thoroughly modern design and is written in JavaScript."}'],
   'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));


INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
    ('MKT000025', 'VestaCP', 'vertice', 'COLLABORATION', '6',
     'active', 'vestacp.png', 'https://vestacp.com', ['{"key":"port.http", "value":"8083"}'],['{"key":"oneclick", "value":"yes"}'],[],[],[],
     ['{"key":"7", "value": "The Vesta Control Panel is a free, open source website control panel with website, email, database, and DNS functionalities built in"}'],
    'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));


INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
    ('MKT000026', 'Ajenti', 'vertice', 'COLLABORATION', '6',
     'active', 'ajenti.png', 'https://ajenti.com', ['{"key":"port.https", "value":"8000"}'],['{"key":"oneclick", "value":"yes"}'],[],[],[],
     ['{"key":"7.2", "value": "Ajenti is an open source, web-based control panel that can be used for a large variety of server management tasks"}'],
     'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));


-- Custom app section
-- Java, Rails, Php, Python, Node, Play(Scala)

INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
    ('MKT000027', 'Java', 'vertice', 'APP', '2',
     'active', 'java.png', 'http://www.oracle.com/technetwork/java/javaee/overview/index.html', ['{"key":"port.http", "value":"8080"}','{"key":"tomcat_username", "value":"megam"}', '{"key":"tomcat_password", "value":"megam"}'],[],[],[],[],
     ['{"key":"8.x", "value": "The Apache Tomcat® software is an open source implementation of the Java Servlet, JavaServer Pages, Java Expression Language and Java WebSocket technologies."}',
      '{"key":"8.5", "value": "The Apache Tomcat® software is an open source implementation of the Java Servlet, JavaServer Pages, Java Expression Language and Java WebSocket technologies."}',
      '{"key":"9.0", "value": "The Apache Tomcat® software is an open source implementation of the Java Servlet, JavaServer Pages, Java Expression Language and Java WebSocket technologies."}'],
     'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));


INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
     ('MKT000028', 'Rails', 'vertice', 'APP', '2',
      'active', 'rails.png', 'http://rubyonrails.org', ['{"key":"port.http", "value":"8080"}'],[],[],[],[],
      ['{"key":"4.x", "value": "RoR home; full stack, Web application framework optimized for sustainable programming productivity, allows writing sound code by favoring convention over configuration."}',
       '{"key":"5.x", "value": "RoR home; full stack, Web application framework optimized for sustainable programming productivity, allows writing sound code by favoring convention over configuration."}'],
      'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));

INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
     ('MKT000029', 'Play', 'vertice', 'APP', '2',
     'active', 'play.png', 'https://www.playframework.com/documentation/2.5.x/ScalaHome', ['{"key":"port.http", "value":"9000"}'],[],[],[],[],
      ['{"key":"2.4.x", "value": "Play is based on a lightweight, stateless, web-friendly architecture. Play Framework makes it easy to build web applications with Scala."}',
       '{"key":"2.5.x", "value": "Play is based on a lightweight, stateless, web-friendly architecture. Play Framework makes it easy to build web applications with Scala."}'],
      'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));

INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
     ('MKT000030', 'Nodejs', 'vertice', 'APP', '2',
     'active', 'nodejs.png', 'https://nodejs.org/en', ['{"key":"port.http", "value":"9001"}'],[],[],[],[],
      ['{"key":"4.5", "value": "Node.js® is a JavaScript runtime built on Chromes V8 JavaScript engine. Node.js uses an event-driven, non-blocking I/O model that makes it lightweight and efficient."}',
      '{"key": "6.5", "value": "Node.js® is a JavaScript runtime built on Chrome V8 JavaScript engine. Node.js uses an event-driven, non-blocking I/O model that makes it lightweight and efficient."}'],
    'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));


INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
     ('MKT000031', 'PHP', 'vertice', 'APP', '2',
     'active', 'php.png', 'https://secure.php.net', ['{"key":"port.http", "value":"80"}','{"key":"url.suffix", "value":"/current"}' ],[],[],[],[],
      ['{"key":"5.x", "value": "PHP (recursive acronym for PHP: Hypertext Preprocessor) is a widely-used open source general-purpose scripting language that is especially suited for web development and can be embedded into HTML"}'],
    'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));

INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
     ('MKT000032', 'CCPlus', 'vertice', 'APP', '2',
     'active', 'ccplus.png', 'https://isocpp.org', [],[],[],[],[],
      ['{"key":"4.9", "value": "C++ is a general-purpose programming language. It has imperative, object-oriented and generic programming features."}'],
    'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));


INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
     ('MKT000033', 'Python', 'vertice', 'APP', '2',
     'active', 'python.png', 'https://isocpp.org', [],[],[],[],[],
      ['{"key":"2.7", "value": "Python is a clear and powerful object-oriented programming language, comparable to Perl, Ruby, Scheme, or Java."}', '{"key": "3.4.2", "value": "Python is a clear and powerful object-oriented programming language, comparable to Perl, Ruby, Scheme, or Java."}'],
    'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));

-- Containers section

INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
         ('MKT000034', 'DockerContainer', 'vertice', 'CONTAINERS', '4',
         'active', 'dockercontainer.png', 'https://www.docker.com', [],[],[],[],[],
          ['{"key":"1.12.x", "value": "Package your application into a standardized unit for software development. Docker containers wrap a piece of software in a complete filesystem that contains everything needed to run: code, runtime, system tools, system libraries – anything that can be installed on a server."}'],
        'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));


-- Analytics section
-- Hadoop, Spark, SparkNotebook

INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
    ('MKT000035', 'Hadoop', 'vertice', 'ANALYTICS', '5',
     'active', 'hadoop.png', 'https://hadoop.apache.org', ['{"key":"port.http", "value":"50070"}'],['{"key":"oneclick", "value":"yes"}'],[],[],[],
     ['{"key":"2.6.0", "value": "Apache Hadoop is an open-source software framework for distributed storage and distributed processing of very large data sets on computer clusters built from commodity hardware."}'],
     'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));


INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
     ('MKT000036', 'Spark', 'vertice', 'ANALYTICS', '5',
      'active', 'spark.png', 'https://spark.apache.org', ['{"key":"port.http", "value":"9004"}'],['{"key":"oneclick", "value":"yes"}'],[],[],[],
      ['{"key":"1.5.0", "value": "Apache® Spark™ is a powerful open source processing engine built around speed, ease of use, and sophisticated analytics."}',
       '{"key":"2.0", "value": "Apache® Spark™ is a powerful open source processing engine built around speed, ease of use, and sophisticated analytics."}'],
      'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));


INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
     ('MKT000037', 'SparkNotebook', 'vertice', 'ANALYTICS', '5',
      'active', 'sparknotebook.png', 'http://spark-notebook.io', ['{"key":"port.http", "value":"9000"}'],['{"key":"oneclick", "value":"yes"}'],[],[],[],
      ['{"key":"0.6.x", "value": "Use Apache Spark straight from the Browser. "}'],
      'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));


-- Bitnami section
-- PM : PIMCore
-- Developer: (Jenkins, DreamFactory, Gitlab, ProcessMake)
-- Marketing: Mautic

INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
   ('MKT000038', 'DreamFactory', 'bitnami', 'COLLABORATION', '6',
   'active', 'dreamfactory.png', 'https://bitnami.com/stack/dreamfactory', ['{"key":"port.http", "value":"80"}', '{"key":"url.suffix", "value":"/"}'],
   ['{"key":"oneclick", "value":"yes"}','{"key":"bitnami_username", "value":"team4megam"}', '{"key":"bitnami_password", "value":"team4megam"}', '{"key":"bitnami_database_password", "value":"team4megam"}',
   '{"key":"bitnami_url", "value": "https://s3-ap-southeast-1.amazonaws.com/megampub/bitnami/Mobile+Development/bitnami-dreamfactory-2.1.1-2-linux-x64-installer.run"}'],
   [],[],[],
   ['{"key":"2.1.1", "value": "DreamFactory is an open source REST API platform for mobile, web, and IoT applications."}'],
   'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));

INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
    ('MKT000039', 'Pimcore', 'bitnami', 'COLLABORATION', '6',
    'active', 'pimcore.png', 'https://bitnami.com/stack/pimcore', ['{"key":"port.http", "value":"80"}', '{"key":"url.suffix", "value":"/"}'],
    ['{"key":"oneclick", "value":"yes"}','{"key":"bitnami_username", "value":"team4megam"}', '{"key":"bitnami_password", "value":"team4megam"}',
    '{"key":"bitnami_url", "value": "https://s3-ap-southeast-1.amazonaws.com/megampub/bitnami/CMS/bitnami-pimcore-3.1.1-2-linux-x64-installer.run"}'],
    [],[],[],
    ['{"key":"3.1.1", "value": "Pimcore is a powerful multi-channel experience and engagement management (CEM/CXM) platform."}'],
    'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));

INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
    ('MKT000040', 'Jenkins', 'bitnami', 'COLLABORATION', '6',
    'active', 'jenkins.png', 'https://bitnami.com/stack/jenkins', ['{"key":"port.http", "value":"80"}', '{"key":"url.suffix", "value":"/"}'],
    ['{"key":"oneclick", "value":"yes"}','{"key":"bitnami_username", "value":"team4megam"}', '{"key":"bitnami_password", "value":"team4megam"}',
    '{"key":"bitnami_url", "value": "https://s3-ap-southeast-1.amazonaws.com/megampub/bitnami/Continuous+Integration/bitnami-jenkins-1.655-0-linux-x64-installer.run"}'],
    [],[],[],
    ['{"key":"1.655", "value": "It’s widely recognized as the most feature-rich CI available with easy configuration, continuous delivery and continuous integration support, easily test, build and stage your app, and more."}'],
    'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));

INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
    ('MKT000041', 'eXo Platform', 'bitnami', 'COLLABORATION', '6',
    'active', 'exoplatform.png', 'https://bitnami.com/stack/exo-platform-community', ['{"key":"port.http", "value":"80"}', '{"key":"url.suffix", "value":"/"}'],
    ['{"key":"oneclick", "value":"yes"}','{"key":"bitnami_username", "value":"team4megam"}', '{"key":"bitnami_password", "value":"team4megam"}',
    '{"key":"bitnami_url", "value": "https://s3-ap-southeast-1.amazonaws.com/megampub/bitnami/Social+Networking/bitnami-exoplatform-4.3.0-1-linux-x64-installer.run"}'],
    [],[],[],
    ['{"key":"4.3.0", "value": "eXo Platform combines collaboration, calendering, content and document management and other productivity tools with social features to provide a complete enterprise intranet."}'],
    'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));

INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
    ('MKT000042', 'Gitlab', 'bitnami', 'COLLABORATION', '6',
    'active', 'gitlab.png', 'https://bitnami.com/stack/gitlab', ['{"key":"port.http", "value":"80"}', '{"key":"url.suffix", "value":"/"}'],
    ['{"key":"oneclick", "value":"yes"}','{"key":"bitnami_username", "value":"team4megam"}', '{"key":"bitnami_password", "value":"team4megam"}',
    '{"key":"bitnami_url", "value": "https://s3-ap-southeast-1.amazonaws.com/megampub/bitnami/Version+Control/bitnami-gitlab-8.5.7-0-linux-x64-installer.run"}'],
    [],[],[],
    ['{"key":"8.5.7", "value": "The GitLab open source edition is a developer tool that allows users to collaborate on code, create new projects, manage repositories, and perform code reviews."}'],
    'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));

INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
    ('MKT000043', 'Mautic', 'bitnami', 'COLLABORATION', '6',
    'active', 'mautic.png', 'https://bitnami.com/stack/mautic', ['{"key":"port.http", "value":"80"}', '{"key":"url.suffix", "value":"/"}'],
    ['{"key":"oneclick", "value":"yes"}','{"key":"bitnami_username", "value":"team4megam"}', '{"key":"bitnami_password", "value":"team4megam"}',
    '{"key":"bitnami_url", "value": "https://s3-ap-southeast-1.amazonaws.com/megampub/bitnami/Marketing+Automation/bitnami-mautic-1.3.1-0-linux-x64-installer.run"}'],
    [],[],[],
    ['{"key":"1.3.1", "value": "Mautic is marketing automation software for everyone.Mautic is free and open source, and can be easily integrated with third-party applications through a REST API and web-hooks."}'],
    'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));

INSERT INTO marketplaces (id, flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz, created_at, updated_at) values
    ('MKT000044', 'ProcessMaker Open Source Edition', 'bitnami', 'COLLABORATION', '6',
    'active', 'processmaker.png', 'https://bitnami.com/stack/processmaker',
    ['{"key":"port.http", "value":"80"}', '{"key":"url.suffix", "value":"/"}'],
    ['{"key":"oneclick", "value":"yes"}','{"key":"bitnami_username", "value":"team4megam"}', '{"key":"bitnami_password", "value":"team4megam"}',
    '{"key":"bitnami_url", "value": "https://s3-ap-southeast-1.amazonaws.com/megampub/bitnami/BPM/bitnami-processmaker-3.0.1.7-0-linux-x64-installer.run"}'],
    [],[],[],
    ['{"key":"3.0.1.7", "value": "ProcessMaker is an open source Business Process Management (BPM) platform designed to automate workflows and improve business efficiency"}'],
    'Megam::MarketPlace', toTimestamp(now()), toTimestamp(now()));
