# Apache Spark with Scala Training

Simple demonstration of deploying a Spark Driver program to a Spark cluster 

Steps:

1. sbt package
2. Make sure your cluster is running and issue something similar to following:

```
~/Development/spark-1.6.1-bin-hadoop2.4/bin/spark-submit --class "SparkPi" --master spark://todd-mcgraths-macbook-pro.local:7077 ./target/scala-2.10/spark-sample_2.10-1.0.jar
```

replace `master` variable with appropriate value for your environment

call `spark-submit` with path appropriate for your environment



