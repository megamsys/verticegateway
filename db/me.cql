-- Schema for minified edition

use vertice;

-- Nuke stuff that is there in marketplaces

DELETE FROM marketplaces WHERE settings_name IN ('vertice', 'bitnami');

-- Machines
-- Ubuntu, Fedora, CentOS,  CoreOS, Windows, DockerMachine


INSERT INTO marketplaces (flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz) values
    ('Ubuntu', 'vertice', 'TORPEDO', '1',
     'active' 'ubuntu.png', 'http://www.ubuntu.com/server', [],[],[],[],[],
     ['{"key":"14.04","value":"Ubuntu is a Debian-based Linux operating system. Trusty Tahr is the Ubuntu codename for version 14.04 LTS of the Ubuntu Linux-based operating system."}',
      '{"key":"16.04", "value":"Ubuntu is a Debian-based Linux operating system. Xenial Xerus is the Ubuntu codename for version 16.04 LTS of the Ubuntu Linux-based operating system."}'],
     'Megam::MarketPlace')

INSERT INTO marketplaces (flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz) values
    ('Debian', 'vertice', 'TORPEDO', '1',
     'active' 'debian.png', 'https://www.debian.org/releases/stable/', [],[],[],[],[],
     ['{"key":"8.5","value":"Debian is a Unix-like computer operating system that is composed entirely of free software, most of which is under the GNU General Public License, and packaged by a group of individuals called the Debian Project."}'],
     'Megam::MarketPlace')

INSERT INTO marketplaces (flavor, provided_by, cattype, catorder, status, image, url, envs, options,  inputs, outputs, acl_policies, plans, json_claz) values
    ('CoreOS', 'vertice', 'TORPEDO', '1',
     'active' 'coreos.png', 'https://coreos.com/why/', [],[],[],[],[],
     ['{"key":"latest", "value": "CoreOS is a lightweight Linux operating system designed for clustered deployments providing automation, security, and scalability for your most critical applications."}'],
     'Megam::MarketPlace')

INSERT INTO marketplaces (flavor, provided_by, cattype, catorder, status, image, url, envs, options,  inputs, outputs, acl_policies, plans, json_claz) values
    ('CentOS', 'vertice', 'TORPEDO', '1',
     'active' 'centos.png', 'https://www.centos.org', [],[],[],[],[],
     ['{"key":"7.2", "value": "CentOS is a Linux distribution that attempts to provide a free, enterprise-class, community-supported computing platform which aims to be functionally compatible with its upstream source, Red Hat Enterprise Linux (RHEL)."}',
     '{"key" :"6.8", "value": "CentOS is a Linux distribution that attempts to provide a free, enterprise-class, community-supported computing platform which aims to be functionally compatible with its upstream source, Red Hat Enterprise Linux (RHEL)."}'],
    'Megam::MarketPlace')

INSERT INTO marketplaces (flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz) values
     ('Fedora', 'vertice', 'TORPEDO', '1',
      'active' 'fedora.png', 'https://getfedora.org/en/server/', [], [],[],[],[],
      ['{"key":"24", "value": "Fedora /fᵻˈdɒr.ə/ (formerly Fedora Core) is an operating system based on the Linux kernel, developed by the community-supported Fedora Project and sponsored by Red Hat."}'],
       'Megam::MarketPlace')


INSERT INTO marketplaces (flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz) values
    ('Windows', 'vertice', 'TORPEDO', '1',
     'active' 'windows.png', 'https://www.microsoft.com/en-us/cloud-platform/windows-server-2012-r2', [], [],[],[],[],
     ['{"key":"2008_R2", "value": "Windows Server 2008 R2 STANDARD EVALUATION 180 days builds on the award-winning foundation of Windows Server 2008, expanding existing technology and adding new features to enable organizations to increase the reliability and flexibility of their server infrastructures."}',
      '{"key":"2012_R2", "value": "Windows Server 2012 R2 STANDARD EVALUATION 180 days brings our experience delivering global-scale cloud services into your infrastructure with features and enhancements in virtualization, management, storage, networking, virtual desktop infrastructure, access and information protection, and the web and application platform."}'],
    'Megam::MarketPlace')


INSERT INTO marketplaces (flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz) values
     ('DockerMachine', 'vertice', 'TORPEDO', '1',
     'active' 'dockermachine.png', 'https://docs.docker.com/machine/overview', ['{"key":"port.tcp", "value":"2376"}'], [], [],[],[],
     ['{"key":"1.12", "value": "Install Docker Engine on virtual hosts, and manage the hosts with docker-machine"}'],
    'Megam::MarketPlace')


INSERT INTO marketplaces (flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz) values
    ('FreeBSD', 'vertice', 'TORPEDO', '1',
     'active' 'freebsd.png', 'https://www.freebsd.org/', [],[],[],[],[],
     ['{"key":"10.3", "value": "FreeBSD is an advanced computer operating system used to power modern servers, desktops, and embedded platforms."}'],
    'Megam::MarketPlace')


-- Common collaboration software
-- Apache2, PostgreSQL, MySQL, Cassandra, Riak, RabbitMQ, Redis


INSERT INTO marketplaces (flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz) values
    ('Apache', 'vertice', 'SERVICE', '3',
     'active' 'apache.png', 'https://www.apache.org', ['{"key":"port.http", "value":"80"}'],[],[],[],[],
     ['{"key":"2.4.18", "value": "Apache is the most widely used web server software. Developed and maintained by Apache Software Foundation."}'],
    'Megam::MarketPlace')


INSERT INTO marketplaces (flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz) values
    ('CouchDB', 'vertice', 'SERVICE', '3',
     'active' 'couchdb.png', 'https://couchdb.apache.org', ['{"key":"port.http", "value":"5984"}', '{"key":"username", "value": "couchdb"}'],[],[],[],[],
     ['{"key":"1.6.1", "value": "CouchDB is a database that completely embraces the web. Store your data with JSON documents. Access your documents and query your indexes with your web browser, via HTTP. Index, combine, and transform your documents with JavaScript."}'],
    'Megam::MarketPlace')


INSERT INTO marketplaces (flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz) values
    ('PostgreSQL', 'vertice', 'SERVICE', '3',
     'active' 'postgres.png', 'https://postgresql.org', ['{"key":"port.tcp", "value":"5432"}', '{"key":"username", "value": "postgres"}',  '{"key":"password", "value":"megam"}'],[],[],[],[],
     ['{"key":"9.5", "value": "PostgreSQL (pronounced "post-gress-Q-L") is an open source relational database management system ( DBMS ) developed by a worldwide team of volunteers."}'],
    'Megam::MarketPlace')


INSERT INTO marketplaces (flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz) values
    ('Riak', 'vertice', 'SERVICE', '3',
     'active' 'riak.png', 'http://docs.basho.com', ['{"key":"port.http", "value":"8098"}'],[],[],[],[],
     ['{"key":"2.1.4", "value": "Riak KV is a distributed NoSQL database that is highly available, scalable and easy to operate. It automatically distributes data across the cluster to ensure fast performance and fault-tolerance."}'],
     'Megam::MarketPlace')


INSERT INTO marketplaces (flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz) values
     ('Redis', 'vertice', 'SERVICE', '3',
      'active' 'redis.png', 'https://redis.io', ['{"key":"port.tcp", "value":"6937"}'],[],[],[],[],
      ['{"key":"2.8.4", "value": "Redis is an open source (BSD licensed), in-memory data structure store, used as database, cache and message broker."}'],
      'Megam::MarketPlace')


INSERT INTO marketplaces (flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz) values
       ('RabbitMQ', 'vertice', 'SERVICE', '3',
        'active' 'rabbitmq.png', 'https://www.rabbitmq.com', ['{"key":"port.http", "value":"15672"}', '{"key":"username", "value":"guest"}', '{"key":"password", "value":"guest"}'],[],[],[],[],
        ['{"key":"3.5.x", "value": "RabbitMQ is open source message broker software (sometimes called message-oriented middleware) that implements the Advanced Message Queuing Protocol (AMQP)."}'],
        'Megam::MarketPlace')

INSERT INTO marketplaces (flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz) values
        ('Mysql', 'vertice', 'SERVICE', '3',
        'active' 'mysql.png', 'https://dev.mysql.com/', ['{"key":"port.tcp", "value":"3306"}', '{"key":"username", "value":"root"}', '{"key":"password", "value":"megam"}'],[],[],[],[],
        ['{"key":"5.x", "value": "MySQL, the most popular Open Source SQL database management system, is developed, distributed, and supported by Oracle Corporation."}'],
        'Megam::MarketPlace')


-- Zarafa, OwnCloud, WordPress, Moodle, PrestaShop,MediaWiki
-- Ghost, Redmine, Discourse,Agenti,VestaCP

INSERT INTO marketplaces (flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz) values
        ('Zarafa', 'vertice', 'COLLABORATION', '6',
        'active' 'zarafa.png', 'https://www.zarafa.com/', ['{"key":"port.http", "value":"80"}', '{"key":"url.append", "value":"/webaccess"}'], ['{"key":"oneclick", "value":"yes"}'],[],[],[],
        ['{"key":"7.2", "value": "Zarafa gives you the tools that help boost your day-to-day communication. File sharing, web meetings and business apps like email and calendar are integrated in one suite."}'],
        'Megam::MarketPlace')


INSERT INTO marketplaces (flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz) values
      ('Owncloud', 'vertice', 'COLLABORATION', '6',
      'active' 'owncloud.png', 'https://owncloud.org/', ['{"key":"port.http", "value":"80"}', '{"key":"url.append", "value":"/"}',
      '{"key":"bitnami_owncloud_site", "value":"127.0.0.1"}'],
      ['{"key":"oneclick", "value":"yes"}','{"key":"bitnami_username", "value":"team4megam"}', '{"key":"bitnami_password", "value":"team4megam"}',
      '{"key":"bitnami_url", "value":"https://s3-ap-southeast-1.amazonaws.com/megampub/bitnami/MediaSharing/bitnami-owncloud-9.1.3-1-linux-x64-installer.run"}'],
      [],[],[],
      ['{"key":"9.1", "value": "Access your data from all your devices, on an open platform you can extend and modify."}'],
      'Megam::MarketPlace')


INSERT INTO marketplaces (flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz) values
      ('WordPress', 'bitnami', 'COLLABORATION', '6',
      'active' 'wordpress.png', 'https://bitnami.com/stack/wordpress', ['{"key":"port.http", "value":"80"}', '{"key":"url.append", "value":"/"}'],
      ['{"key":"oneclick", "value":"yes"}','{"key":"bitnami_username", "value":"team4megam"}', '{"key":"bitnami_password", "value":"team4megam"}',
      '{"key":"bitnami_url", "value":"https://s3-ap-southeast-1.amazonaws.com/megampub/bitnami/Blog/bitnami-wordpress-4.4.2-3-linux-x64-installer.run"}'],
      [],[],[],
      ['{"key":"4.4.2", "value": "WordPress is the web publishing platforms for building blogs and websites."}'],
      'Megam::MarketPlace')

INSERT INTO marketplaces (flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz) values
      ('Moodle', 'bitnami', 'COLLABORATION', '6',
      'active' 'moodle.png', 'https://bitnami.com/stack/moodle', ['{"key":"port.http", "value":"80"}', '{"key":"url.append", "value":"/"}'],
       ['{"key":"oneclick", "value":"yes"}','{"key":"bitnami_username", "value":"team4megam"}', '{"key":"bitnami_password", "value":"team4megam"}',
       '{"key":"bitnami_url", "value":"https://s3-ap-southeast-1.amazonaws.com/megampub/bitnami/eLearning/bitnami-moodle-3.0.3-0-linux-x64-installer.run"}'],
        [],[],[],
        ['{"key":"3.0.3", "value": "Moodle is a very popular open source learning management solution (LMS) for the delivery of elearning courses and programs."}'],
        'Megam::MarketPlace')

INSERT INTO marketplaces (flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz) values
      ('PrestaShop', 'bitnami', 'COLLABORATION', '6',
      'active' 'prestashop.png', 'https://bitnami.com/stack/prestashop', ['{"key":"port.http", "value":"80"}', '{"key":"url.append", "value":"/"}', '{"key":"bitnami_prestashop_site", "value":"127.0.0.1"}'],
      ['{"key":"oneclick", "value":"yes"}','{"key":"bitnami_username", "value":"team4megam"}', '{"key":"bitnami_password", "value":"team4megam"}',
       '{"key":"bitnami_url", "value":"https://s3-ap-southeast-1.amazonaws.com/megampub/bitnami/e-Commerce/bitnami-prestashop-1.6.1.4-0-linux-x64-installer.run"}'],
        [],[],[],
        ['{"key":"1.6.1.4", "value": "PrestaShop has perhaps the most unique and interesting out of the box experience of any ecommerce platform "}'],
      'Megam::MarketPlace')


INSERT INTO marketplaces (flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz) values
      ('MediaWiki', 'bitnami', 'COLLABORATION', '6',
      'active' 'mediawiki.png', 'https://bitnami.com/stack/mediawiki', ['{"key":"port.http", "value":"80"}', '{"key":"url.append", "value":"/"}'],
      ['{"key":"oneclick", "value":"yes"}','{"key":"bitnami_username", "value":"team4megam"}', '{"key":"bitnami_password", "value":"team4megam"}',
       '{"key":"bitnami_url", "value":"https://s3-ap-southeast-1.amazonaws.com/megampub/bitnami/Wiki/bitnami-mediawiki-1.26.2-1-linux-x64-installer.run"}'],
        [],[],[],
        ['{"key":"1.26.2", "value": "MediaWiki is the engine behind Wikipedia, the behemoth website with millions of articles written by millions of users and translated into dozens of languages."}'],
      'Megam::MarketPlace')


INSERT INTO marketplaces (flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz) values
    ('Ghost', 'bitnami', 'COLLABORATION', '6',
    'active' 'ghost.png', 'https://bitnami.com/stack/ghost', ['{"key":"port.http", "value":"80"}', '{"key":"url.append", "value":"/"}'],
    ['{"key":"oneclick", "value":"yes"}','{"key":"bitnami_username", "value":"team4megam"}', '{"key":"bitnami_password", "value":"team4megam"}',
   '{"key":"bitnami_url", "value":"https://s3-ap-southeast-1.amazonaws.com/megampub/bitnami/Blog/bitnami-ghost-0.7.8-0-linux-x64-installer.run"}'],
    [],[],[],
    ['{"key":"0.7.8", "value": "Ghost is built on Node.js and is extremely easy to configure and customize."}'],
    'Megam::MarketPlace')


INSERT INTO marketplaces (flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz) values
    ('Redmine', 'bitnami', 'COLLABORATION', '6',
    'active' 'redmine.png', 'https://bitnami.com/stack/redmine', ['{"key":"port.http", "value":"80"}', '{"key":"url.append", "value":"/"}'],
    ['{"key":"oneclick", "value":"yes"}','{"key":"bitnami_username", "value":"team4megam"}', '{"key":"bitnami_password", "value":"team4megam"}',
    '{"key":"bitnami_url", "value":"https://s3-ap-southeast-1.amazonaws.com/megampub/bitnami/Bug+Tracking/bitnami-redmine-3.2.1-0-linux-x64-installer.run"}'],
    [],[],[],
    ['{"key":"3.2.1", "value": "Redmine is a very popular Rails-based open source bug tracker to help you manage issues and tasks for multiple projects."}'],
   'Megam::MarketPlace')

INSERT INTO marketplaces (flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz) values
   ('Discourse', 'bitnami', 'COLLABORATION', '6',
   'active' 'discourse.png', 'https://bitnami.com/stack/discourse', ['{"key":"port.http", "value":"80"}', '{"key":"url.append", "value":"/"}'],
   ['{"key":"oneclick", "value":"yes"}','{"key":"bitnami_username", "value":"team4megam"}', '{"key":"bitnami_password", "value":"team4megam"}',
   '{"key":"bitnami_url", "value": "https://s3-ap-southeast-1.amazonaws.com/megampub/bitnami/Forum/bitnami-discourse-1.4.6-0-linux-x64-installer.run"}'],
   [],[],[],
   ['{"key":"1.4.6", "value": "Discourse is the next-next-generation community forum platform. Discourse has a thoroughly modern design and is written in JavaScript."}'],


INSERT INTO marketplaces (flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz) values
    ('VestaCP', 'vertice', 'COLLABORATION', '6',
     'active' 'vestacp.png', 'https://vestacp.com', ['{"key":"port.http", "value":"8083"}'],['{"key":"oneclick", "value":"yes"}'],[],[],[],
     ['{"key":"7", "value": "The Vesta Control Panel is a free, open source website control panel with website, email, database, and DNS functionalities built in"}'],
    'Megam::MarketPlace')


INSERT INTO marketplaces (flavor, provided_by, cattype, catorder, status, image, url, envs, options, inputs, outputs, acl_policies, plans, json_claz) values
    ('Ajenti', 'vertice', 'COLLABORATION', '6',
     'active' 'ajenti.png', 'https://ajenti.com', ['{"key":"port.https", "value":"8000"}'],['{"key":"oneclick", "value":"yes"}'],[],[],[],
     ['{"key":"7.2", "value": "Ajenti is an open source, web-based control panel that can be used for a large variety of server management tasks"}'],
     'Megam::MarketPlace')
