# Deployment for Analytics Project

## Environment Setup

### CentOS 6.6

### Disable selinux

### Disk partition

	[root@edp06 ~]# lsblk
	NAME   MAJ:MIN RM  SIZE RO TYPE MOUNTPOINT
	sda      8:0    0  1.8T  0 disk 
	├─sda1   8:1    0  200M  0 part /boot/efi
	├─sda2   8:2    0  200M  0 part /boot
	├─sda3   8:3    0   32G  0 part [SWAP]
	├─sda4   8:4    0   30G  0 part /
	├─sda5   8:5    0   30G  0 part /usr
	├─sda6   8:6    0   30G  0 part /var
	├─sda7   8:7    0   20G  0 part /data/zookeeper
	└─sda8   8:8    0  1.7T  0 part /data/hdfs
	
	mount -o noatime -o remount /data/hdfs

### Security disable SSH password authentication

## Performance Tuning

[Optimizaing Performance in CDH](http://www.cloudera.com/content/www/en-us/documentation/enterprise/latest/topics/cdh_admin_performance.html)

[Disabling Transparent Hugepage Compaction](http://www.cloudera.com/content/www/en-us/documentation/enterprise/latest/topics/cdh_admin_performance.html#xd_583c10bfdbd326ba-7dae4aa6-147c30d0933--7fd5__section_hw3_sdf_jq)

Disable THP at boot time

	 if test -f /sys/kernel/mm/redhat_transparent_hugepage/enabled; then
	       echo never > /sys/kernel/mm/redhat_transparent_hugepage/enabled
	          fi
	 if test -f /sys/kernel/mm/redhat_transparent_hugepage/defrag; then
	     echo never > /sys/kernel/mm/redhat_transparent_hugepage/defrag
	         fi

Or
	
	echo never > /sys/kernel/mm/redhat_transparent_hugepage/defrag

Or edit /etc/grub.conf

	kernel /vmlinuz-2.6.32-504.el6.x86_64 ro root=UUID=d0bb5e81-d23e-4c7a-9141-02b859923722 rd_NO_LUKS  KEYBOARDTYPE=pc KEYTABLE=us rd_NO_MD crashkernel=128M LANG=zh_CN.UTF-8 rd_NO_LVM rd_NO_DM rhgb quiet transparent_hugepage=never

[Setting the vm.swappiness Linux Kernel Parameter](http://www.cloudera.com/content/www/en-us/documentation/enterprise/latest/topics/cdh_admin_performance.html#xd_583c10bfdbd326ba-7dae4aa6-147c30d0933--7fd5__section_xpq_sdf_jq)

	sysctl -w vm.swappiness=0

[Improving Performance in Shuffle Handler and IFile Reader](http://www.cloudera.com/content/www/en-us/documentation/enterprise/latest/topics/cdh_admin_performance.html#xd_583c10bfdbd326ba-7dae4aa6-147c30d0933--7fd5__section_nt5_sdf_jq)

[Best Practices for MapReduce Configuration](http://www.cloudera.com/content/www/en-us/documentation/enterprise/latest/topics/cdh_admin_performance.html#xd_583c10bfdbd326ba-7dae4aa6-147c30d0933--7fd5__best-mapreduce)

[Tips and Best Practices for Jobs](http://www.cloudera.com/content/www/en-us/documentation/enterprise/latest/topics/cdh_admin_performance.html#xd_583c10bfdbd326ba-7dae4aa6-147c30d0933--7fd5__section_m4h_tdf_jq)


## Build Repo
repo

## Install
salt-master salt-minion

docker

[Install HBase](http://www.cloudera.com/content/www/en-us/documentation/enterprise/latest/topics/cdh_ig_hbase_config.html)

3. deploy postgresql
salt 'edp01.esse.io' state.sls postgresql devops
4. deploy zookeeper
salt -G 'roles:zookeeper' state.sls zookeeper devops


## Topology

	nodes:
	    edp06.esse.io:
	      roles:
	        - hadoop-namenode
	        - hadoop-datanode
	    edp05.esse.io:
	      roles:
	        - hadoop-datanode
	        - zookeeper
	    edp04.esse.io:
	      roles:
	        - hadoop-datanode
	        - spark-worker
	    edp03.esse.io:
	      roles:
	        - hadoop-datanode
	        - spark-worker
	    edp02.esse.io:
	      roles:
	        - hadoop-datanode
	    edp01.esse.io:
	      roles:
	        - hadoop-datanode
	        - postgresql

	Storm:  nimbus.host: "edp06"  supervisor hosts: edp02,edp03,edp04,edp01
	hbase: master edp01, regionservers edp02.edp03,edp04,edp05,edp06

## Install HBase

http://www.cloudera.com/content/www/en-us/documentation/enterprise/latest/topics/cdh_ig_hbase_config.html
