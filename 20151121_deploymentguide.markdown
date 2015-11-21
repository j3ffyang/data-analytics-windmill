# Data Analytics PoC Deployment Guide

## Hardware Setup
* BIOS
* Network Bonding

## Environment Setup
### Disk RAID 0
### CentOS 6.7 x86_64
### Partition ext4fs

      [root@poc1 ~]# lsblk    
      NAME   MAJ:MIN RM  SIZE RO TYPE MOUNTPOINT    
      sr0     11:0    1 1024M  0 rom    
      sda      8:0    0  2.2T  0 disk    
      ├─sda1   8:1    0  512M  0 part /boot    
      ├─sda2   8:2    0 79.1G  0 part [SWAP]    
      ├─sda3   8:3    0 29.3G  0 part /    
      ├─sda4   8:4    0 29.3G  0 part /tmp    
      ├─sda5   8:5    0 29.3G  0 part /var    
      ├─sda6   8:6    0  100G  0 part /usr    
      ├─sda7   8:7    0 19.5G  0 part     /data/zookeeper    
      └─sda8   8:8    0    2T  0 part /data    

### Hosts    
    cat /etc/hosts

      127.0.0.1   localhost localhost.localdomain localhost4 localhost4.localdomain4     
      ::1         localhost localhost.localdomain localhost6 localhost6.localdomain6

      192.168.210.101 poc1 poc1.esse.io     
      192.168.210.102 poc2 poc2.esse.io     
      192.168.210.103 poc3 poc3.esse.io     
      192.168.210.104 poc4 poc4.esse.io     
      192.168.210.105 poc5 poc5.esse.io     
      192.168.210.106 poc6 poc6.esse.io     


### Speed up sshd
    UseDNS no   
    GSSAPIAuthentication no     

### Stop Unused Daemons

     for i in 2 3 4 5 6; do ssh poc$i "chkconfig autofs off"; done
     for i in 2 3 4 5 6; do ssh poc$i "chkconfig cups off"; done
     for i in 2 3 4 5 6; do ssh poc$i "chkconfig iptables off"; done

     for i in 2 3 4 5 6; do ssh poc$i "chkconfig ip6tables off"; done
     for i in 2 3 4 5 6; do ssh poc$i "chkconfig netfs off"; done
     for i in 2 3 4 5 6; do ssh poc$i "chkconfig nfslock off"; done
     for i in 2 3 4 5 6; do ssh poc$i "chkconfig postfix off"; done
     for i in 2 3 4 5 6; do ssh poc$i "chkconfig rpcbind off"; done
     for i in 2 3 4 5 6; do ssh poc$i "chkconfig rpcgssd off"; done

     for i in 1 2 3 4 5 6; do ssh poc$i '/etc/init.d/rpcgssd stop; echo $HOSTNAME'; done

### Enable “set -o vi” in /etc/bashrc (optional)
     for i in 1 2 3 4 5 6; do ssh poc$i "echo 'set -o vi' >> /etc/bashrc"; done

      Update “transparent_hugepage” in Kernel
     /etc/grub.conf

     add 'transparent_hugepage=never’ at end of quite

### Update /etc/sysctl.conf
     vm.swappiness=0

### Update /etc/fstab
     need option ‘defaults,noatime’ when mount FS.
     reboot all the machines

### Create Local repos
     yum install createrepo httpd*
     cp $PATH/Packages/* $REPO_PATH/
     cd $REPO_PATH; createrepo .

     ln -s $REPO_PATH /var/www/html/centos_repo

     /etc/init.d/httpd start

     [root@poc1 os_orig_repos]# cat /etc/yum.repos.d/iso.repo
     [iso]
     #baseurl=file:///var/www/html/centos6_repo
     baseurl=http://192.168.210.101/centos6_repo
     enabled=1
     gpgcheck=0

     yum clean all; yum update

     Remove original repos and copy repo to all other hosts
     for i in 2 3 4 5 6; do ssh poc$i "mv /etc/yum.repos.d/* /tmp"; done

     for i in 2 3 4 5 6; do scp /etc/yum.repos.d/* poc$i:/etc/yum.repos.d/; done

### Setup NTP on all machines
     On NTP Server, edit /etc/ntp.conf

     server poc1.esse.io iburst
     #server 1.centos.pool.ntp.org iburst
     #server 2.centos.pool.ntp.org iburst
     #server 3.centos.pool.ntp.org iburst

     server  127.127.1.0 # local clock
     fudge   127.127.1.0 stratum 10

     Distribute ntp.conf to all other NTP client then restart all ntp daemons on all boxes

     chkconfig ntpd on

  ### JDK
#### Download

     for i in 1 2 3 4 5 6; do ssh poc$i "cd /tmp; wget http://192.168.210.101/repos/java/jdk-8u40-linux-x64.rpm"; done

#### Install JDK
     for i in 1 2 3 4 5 6; do ssh poc$i "cd /tmp; yum localinstall jdk-8u40-linux-x64.rpm -y"; done

#### Create a Softlink
     ln -s /usr/java /usr/jdk64
     for i in 2 3 4 5 6; do ssh poc$i "ln -s /usr/java /usr/jdk64"; done

##  Hortonworks Data Platform Manager Setup
###  Install PostgreSQL and Ambari
####  Install docker-engine
     yum localinstall docker-engine-1.7.1-1.el6.x86_64.rpm
####  Import PostgreSQL and Ambari images
     [root@poc1 docker-container]# ls
     ambari.tgz  postgresql.tar  zookeeper.tar
     [root@poc1 docker-container]# docker load -i postgresql.tar
     [root@poc1 docker-container]# docker load -i ambari.tgz

     docker run -d --restart=always -v /data/postgresql:/var/lib/postgresql -p 5432:5432 -e DB_NAME=hive_meta,ambari_server -e DB_USER=dbuser -e DB_PASS=$PASSWD --name postgresql sameersbn/postgresql:9.4-2

     Edit /data/postgresql/9.4/main/pg_hba.conf
     host    all       all       192.168.210.0/24           trust

     [root@poc1 ~]# vi /data/postgresql/9.4/main/pg_hba.conf
     [root@poc1 ~]# docker ps -a
     CONTAINER ID        IMAGE                        COMMAND             CREATED             STATUS              PORTS                    NAMES
     7479aca88155        sameersbn/postgresql:9.4-2   "/start"            13 hours ago        Up 28 minutes       0.0.0.0:5432->5432/tcp   postgresql          
     [root@poc1 ~]# docker restart postgresql
     postgresql

#### Install docker Ambari container (optional)
     docker run -d -p 8080:8080 -p 8440:8440 -p 8441:8441 --name ambari-server -h ambari-test.esse.io -e POSTGRES_SERVER=192.168.210.101 -e POSTGRES_PORT=5432 -e POSTGRES_DB=ambari_server -e POSTGRES_USER=dbuser -e POSTGRES_PWS=$PASSWD --add-host='poc1.esse.io:192.168.210.101' --add-host='poc2.esse.io:192.168.210.102' --add-host='poc3.esse.io:192.168.210.103' --add-host='poc4.esse.io:192.168.210.104' --add-host='poc5.esse.io:192.168.210.105' --add-host='poc6.esse.io:192.168.210.106' ambari:0.0.1

####  Check Ambari Starting (optional)
     docker logs -tf ambari-server
###  Install Ambari (native and recommended)
     yum install ambari-server

     ambari-server setup -s \
           --database=postgres \
           --databasehost=$POSTGRES_SERVER \
           --databaseport=$POSTGRES_PORT \
           --databasename=$POSTGRES_DB \
           --databaseusername=$POSTGRES_USER \
           --databasepassword=$POSTGRES_PWS \
           --java-home $JAVA_HOME

     [root@poc1 postgresql]# POSTGRES_SERVER=192.168.210.101
     [root@poc1 postgresql]# POSTGRES_DB=ambari_server
     [root@poc1 postgresql]# POSTGRES_USER=dbuser

     psql -d $POSTGRES_DB -h $POSTGRES_SERVER -U $POSTGRES_USER -f /var/lib/ambari-server/resources/Ambari-DDL-Postgres-CREATE.sql

     ambari-server setup --jdbc-db=postgres –jdbc-driver=/data/poc/ARTIFACTS/postgresql-9.4-1201.jdbc4.jar


###  Verify Ambari
     http://192.168.210.101:8080
     -admin:admin

####  Trick 1 - “\u2028”
     During starting Ambari Metrics Collector, the following error may appear
     “UnicodeEncodeError: 'ascii' codec can't encode character u'\u2028' in position 492: ordinal not in range(128)”

     Workaround of getting rid of “\u2028”

     Check whether “ambari_server” database contains such char

     psql -d ambari_server -h localhost -U dbuser -W -c "select config_id, config_data,type_name from clusterconfig where type_name='ams-site';" > /tmp/test.log

     If it does appear (in /tmp/test.log), update the database


     psql -d ambari_server -h localhost -U dbuser -W -c "update clusterconfig set config_data='{\"timeline.metrics.host.aggregate.splitpoints\":\"Total Tasks,cpu_num,default.StartupProgress.LoadingFsImageTotal,dfs.FSNamesystem.ScheduledReplicationBlocks,dfs.datanode.HeartbeatsAvgTime,dfs.datanode.WriteBlockOpNumOps,dfs.namenode.RenameSnapshotOps,ipc.IPC.ProcessCallTime_num_ops,jvm.JvmMetrics.GcTimeMillis,jvm.JvmMetrics.MemNonHeapUsedM,kafka.controller.ControllerStats.LeaderElectionRateAndTimeMs.mean,kafka.network.RequestChannel.ResponseQueueSize.processor.2,kafka.network.RequestMetrics.RequestsPerSec.request.Heartbeat.15MinuteRate,kafka.network.RequestMetrics.RequestsPerSec.request.OffsetFetch.5MinuteRate,kafka.network.RequestMetrics.RequestsPerSec.request.UpdateMetadata.meanRate,kafka.network.SocketServer.NetworkProcessorAvgIdlePercent.5MinuteRate,kafka.server.BrokerTopicMetrics.FailedProduceRequestsPerSec.5MinuteRate,mapred.ShuffleMetrics.ShuffleOutputsFailed,master.Balancer.BalancerCluster_median,master.FileSystem.MetaHlogSplitTime_75th_percentile,mem_shared,metricssystem.MetricsSystem.Sink_timelineAvgTime,read_bps,regionserver.Server.Delete_median,regionserver.Server.Replay_95th_percentile,rpc.RetryCache.NameNodeRetryCache.CacheHit,rpc.rpc.SentBytes,rpcdetailed.rpcdetailed.GetContainerStatusesAvgTime,rpcdetailed.rpcdetailed.StopContainersAvgTime\",\"timeline.metrics.cluster.aggregator.hourly.interval\":\"3600\",\"timeline.metrics.cluster.aggregator.minute.interval\":\"120\",\"timeline.metrics.cluster.aggregator.minute.ttl\":\"2592000\",\"timeline.metrics.cluster.aggregator.minute.checkpointCutOffMultiplier\":\"2\",\"timeline.metrics.host.aggregator.minute.interval\":\"120\",\"timeline.metrics.cluster.aggregator.daily.checkpointCutOffMultiplier\":\"1\",\"timeline.metrics.host.aggregator.hourly.interval\":\"3600\",\"timeline.metrics.service.rpc.address\":\"0.0.0.0:60200\",\"timeline.metrics.service.operation.mode\":\"embedded\",\"timeline.metrics.aggregator.checkpoint.dir\":\"/var/lib/ambari-metrics-collector/checkpoint\",\"timeline.metrics.host.aggregator.minute.disabled\":\"false\",\"timeline.metrics.cluster.aggregate.splitpoints\":\"dfs.datanode.CacheReportsAvgTime,jvm.JvmMetrics.MemHeapMaxM,kafka.network.RequestMetrics.RequestsPerSec.request.Offsets.1MinuteRate,master.Balancer.BalancerCluster_95th_percentile,regionserver.Server.Append_95th_percentile,rpcdetailed.rpcdetailed.GetNewApplicationNumOps\",\"timeline.metrics.cluster.aggregator.minute.disabled\":\"false\",\"timeline.metrics.service.use.groupBy.aggregators\":\"true\",\"timeline.metrics.host.aggregator.minute.checkpointCutOffMultiplier\":\"2\",\"phoenix.query.maxGlobalMemoryPercentage\":\"25\",\"timeline.metrics.service.default.result.limit\":\"5760\",\"timeline.metrics.hbase.compression.scheme\":\"SNAPPY\",\"timeline.metrics.cluster.aggregator.daily.ttl\":\"63072000\",\"timeline.metrics.service.checkpointDelay\":\"60\",\"timeline.metrics.sink.report.interval\":\"60\",\"timeline.metrics.cluster.aggregator.minute.timeslice.interval\":\"30\",\"timeline.metrics.service.webapp.address\":\"0.0.0.0:6188\",\"timeline.metrics.host.aggregator.daily.ttl\":\"31536000\",\"timeline.metrics.host.aggregator.hourly.ttl\":\"2592000\",\"timeline.metrics.host.aggregator.daily.disabled\":\"false\",\"timeline.metrics.cluster.aggregator.daily.interval\":\"86400\",\"timeline.metrics.cluster.aggregator.daily.disabled\":\"false\",\"timeline.metrics.cluster.aggregator.hourly.disabled\":\"false\",\"timeline.metrics.host.aggregator.hourly.disabled\":\"false\",\"timeline.metrics.host.aggregator.minute.ttl\":\"604800\",\"timeline.metrics.service.cluster.aggregator.appIds\":\"datanode,nodemanager,hbase\",\"timeline.metrics.host.aggregator.daily.checkpointCutOffMultiplier\":\"1\",\"timeline.metrics.cluster.aggregator.hourly.checkpointCutOffMultiplier\":\"2\",\"timeline.metrics.service.resultset.fetchSize\":\"2000\",\"timeline.metrics.hbase.data.block.encoding\":\"FAST_DIFF\",\"timeline.metrics.cluster.aggregator.hourly.ttl\":\"31536000\",\"timeline.metrics.sink.collection.period\":\"60\",\"timeline.metrics.host.aggregator.hourly.checkpointCutOffMultiplier\":\"2\",\"timeline.metrics.daily.aggregator.minute.interval\":\"86400\",\"timeline.metrics.host.aggregator.ttl\":\"86400\",\"phoenix.spool.directory\":\"/tmp\"}' where config_id=82;" > /tmp/test.log

     Notice: the value of “config_id” might be diff dynamically.

     Make sure that the value of “config_date” doesn't contain any of “\u2028” character. Then

     /etc/init.d/ambari-server restart
### Configure Ambari
#### Edit Repo in Ambari-Server

     docker exec -it ambari-server bash

     [root@docker-ambari yum.repos.d]# vi ambari.repo  

     #VERSION_NUMBER=2.1.2-377
     [Updates-ambari-2.1.2]
     name=ambari-2.1.2 - Updates
     baseurl=http://192.168.210.101/repos/AMBARI-2.1.2/centos6/
     gpgcheck=1
     gpgkey=http://192.168.210.101/repos/AMBARI-2.1.2/centos6/RPM-GPG-KEY/RPM-GPG-KEY-Jenkins
     enabled=1
     priority=1
