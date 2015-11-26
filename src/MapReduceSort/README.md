# mapreduce sort

# usage
```
java -jar mrsort.jar <hdfs source path> <hdfs output path> <analog sort no> <output int> <output start no> [start time] [end time]
```

# for example
```
java -jar mrsort.jar hdfs://quickstart:8020/user/cloudera/test/lytestdata  hdfs://quickstart:8020/user/cloudera/test/mrsortout 1 10000000 1 "2015-11-21 11:00:00" "2015-11-21 21:00:00"
```