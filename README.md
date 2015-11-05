# Deployment for Analytics Project

## Environment Setup

CentOS 6.6

Disable selinux

Disk partition

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

Security disable SSH password authentication

## Performance Tuning
http://www.cloudera.com/content/www/en-us/documentation/enterprise/latest/topics/cdh_admin_performance.html

### Disable transparent_hugepage > edit /etc/rc.local

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

### Reduce SWAP IO
	sysctl -w vm.swappiness=0

## Build Repo
repo

## Install
salt-master salt-minion

docker

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
