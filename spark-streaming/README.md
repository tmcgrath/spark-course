# Apache Spark with Scala training course

To run:
SPARK_HOME/bin/spark-submit --conf spark.driver.userClassPathFirst=true --class "com.supergloo.SlackStreamingApp" --master spark://MASTER:7077 ./target/scala-2.11/spark-streaming-example-assembly-1.0.jar local[5] YOUR_SLACK_KEY output
