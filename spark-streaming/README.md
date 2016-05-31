# Apache Spark with Scala training course

To run:
SPARK_HOME/bin/spark-submit --conf spark.driver.userClassPathFirst=true --class "com.supergloo.SlackStreamingApp" --master spark://MASTER:7077 ./target/scala-2.11/spark-streaming-example-assembly-1.0.jar local[5] YOUR_SLACK_KEY output

----

To run tests:

    sbt test
    
To run test with coverage and scalastyle checks:

    sbt sanity
    
This will output Warnings and error associated with the code. Also will generate
the coverage report in _target/scala-2.11/scoverage-report_ in HTML format.