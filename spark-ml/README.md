# Apache Spark with Scala training course - Machine Learning (MLlib) example


TODO- change program name, add resources for more language

## What it does?
It performs k-means clustering on text messages streamed from Slack.  It may use an existing k-means model or new training data may be created and used to train.

## How it works?

There are three cases to run the code depending on the config variables passed to the program:

1. If --trainData and --modelLocation config variables are specified, the program will train model based on existing train data in --trainData.  (example trainData is in the input/ directory)

2. If --trainData and not --slackToken, the program will just train model and save it if --modelLocation specified.

3. If --slackToken and --modelLocation specified, the program will load model from the --modelLocation and put it
to streaming app which will be used for near real-time prediction.

## Configuration Option Examples

--numClusters 3 - number of clusters(k)  // we specify 3 in videos because there are 3 languages in input/ directory

--trainData input - data which used for model training

--modelLocation model - directory to save model

--predictOutput output - optional output directory prefix. In not present, the job will only print all data to console


## What to expect:
After job is started, you should see messages retrieved from slack and the cluster id to which this message relates.

## How to run?  

There are two ways to run this code: from sbt console or deploy as a jar.  Deploy to Spark cluster and run as a jar is what is configured in the build.sbt file.

To deploy to a cluster
1. sbt assembly

Train a model first
1. SPARK_HOME/bin/spark-submit --conf spark.driver.userClassPathFirst=true --class "com.supergloo.SlackMLApp" --master MASTER ./target/scala-2.11/spark-ml-example-assembly-1.0.jar --numClusters 3 --trainData input --modelLocation model

Run near-real time prediction
1. SPARK_HOME/bin/spark-submit --conf spark.driver.userClassPathFirst=true --class "com.supergloo.SlackMLApp" --master MASTER ./target/scala-2.11/spark-ml-example-assembly-1.0.jar --slackToken YOUR_SLACK_KEY  --numClusters 3 --modelLocation model 


where you'll need to update SPARK_HOME, the --master var value and YOUR_SLACK_KEY
