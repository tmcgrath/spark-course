# Apache Spark with Scala training course - Machine Learning (MLlib) example


TODO- change program name, add resources for more language

## What it does?
It performs k-means clustering on text messages streamed from Slack.  It may use an existing k-means model or new training data may be created and used to train.

## How it works?

There are three cases to run the code depending on the config variables passed to the program:

1) If --trainData and --slackToken config variables are specified, the program will train model based on existing train data in --trainData.
Save model only if --modelLocation specified. 
2) If --trainData and not --slackToken, the program will just train model and save it if --modelLocation specified.
3) If --slackToken and --modelLocation specified, the program will load model from the --modelLocation and put it
to streaming app which will be used for prediction.

## Configuration options

--numClusters 3 - number of clusters(k)

--trainData input - data which used for model training

--modelLocation model - directory to save model

--predictOutput output - optional output directory prefix. In not present, the job will only print all data to console


## What to expect:
After job is started, you should see messages retrieved from slack and the cluster id to which this message relates.

## How to run?  

There are two ways to run this code: from sbt console or deploy as a jar.  Deploy to Spark cluster and run as a jar is the default.

To deploy to a cluster
1) sbt assembly
2) SPARK_HOME/bin/spark-submit --conf spark.driver.userClassPathFirst=true --class "com.supergloo.SlackMLApp" --master spark://MASTER:7077 ./target/scala-2.11/spark-ml-example-assembly-1.0.jar YOUR_SLACK_KEY  --numClusters 3 --trainData input --modelLocation model --predictOutput output

where you'll need to update SPARK_HOME, the --master var value and YOUR_SLACK_KEY

To run from sbt:
1) update build.sbt file according to comments in the file itself
2) sbt
3) run --master local[5] --slackToken YOUR_SLACK_KEY --numClusters 3 --trainData input --modelLocation model --predictOutput output

where you'll need to update the --master var value and YOUR_SLACK_KEY
