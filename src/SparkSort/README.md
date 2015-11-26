# Spark Sort

# usage
```
spark-submit --class com.ibm.SparkSort --master yarn-client sparksort.jar <hdfs source path> <hdfs output path> <analog sort no> <output int> <output start no> [start time] [end time]
```

# for example
```
spark-submit --class com.ibm.SparkSort --master yarn-client sparksort.jar hdfs://quickstart:8020/user/cloudera/test/lytestdata hdfs://quickstart:8020/user/cloudera/test/kafkasortout 1 10000000 1 "2015-11-21 11:00:00" "2015-11-21 21:00:00"
```
