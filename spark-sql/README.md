# Apache Spark with Scala Training

Demonstrates running a Spark SQL app for Spark Training Course

To run JDBC example:
1) sbt assembly
2) Make sure your cluster is running and issue something similar to following:

~/Development/spark-1.6.1-bin-hadoop2.4/bin/spark-submit --class "com.supergloo.SparkSQLJDBCApp" --master spark://todd-mcgraths-macbook-pro.local:7077 ./target/scala-2.11/spark-sql-examples-assembly-1.0.jar

replace your `master` with value

call `spark-submit` with path appropriate for your environment



