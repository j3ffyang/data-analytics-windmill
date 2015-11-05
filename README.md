# Deployment for Analytics Project

## Environment Setup

CentOS 6.6

disable selinux
disk partition
sshd

	mount -o noatime -o remount /data/hdfs

## Performance Tuning
http://www.cloudera.com/content/www/en-us/documentation/enterprise/latest/topics/cdh_admin_performance.html

### disable transparent_hugepage > edit /etc/rc.local

disable THP at boot time
 if test -f /sys/kernel/mm/redhat_transparent_hugepage/enabled; then
       echo never > /sys/kernel/mm/redhat_transparent_hugepage/enabled
          fi
 if test -f /sys/kernel/mm/redhat_transparent_hugepage/defrag; then
     echo never > /sys/kernel/mm/redhat_transparent_hugepage/defrag
         fi


echo never > /sys/kernel/mm/redhat_transparent_hugepage/defrag

Or edit /etc/grub.conf
vi /etc/grub.conf
[4:27:26 PM] Jian Hua Geng: kernel /vmlinuz-2.6.32-504.el6.x86_64 ro root=UUID=d0bb5e81-d23e-4c7a-9141-02b859923722 rd_NO_LUKS  KEYBOARDTYPE=pc KEYTABLE=us rd_NO_MD crashkernel=128M LANG=zh_CN.UTF-8 rd_NO_LVM rd_NO_DM rhgb quiet transparent_hugepage=never

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