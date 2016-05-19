package com.supergloo

import org.apache.spark.SparkContext
import org.apache.spark.mllib.clustering.KMeansModel
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.util.parsing.json.JSON

/**
  * Spark with Scala course
  */
object SlackStreamingTask {

  def run(sparkContext: SparkContext, slackToken: String, clusters: KMeansModel, predictOutput: String) {
    val ssc = new StreamingContext(sparkContext, Seconds(5))
    val dStream = ssc.receiverStream(new SlackReceiver(slackToken))
    
    val stream = dStream //create stream of events from the Slack... but filter and marshall to JSON stream data 
      .filter(JSON.parseFull(_).get.asInstanceOf[Map[String, String]]("type") == "message") // get only message events
      .map(JSON.parseFull(_).get.asInstanceOf[Map[String, String]]("text")) // extract message text from the event

    val kmeanStream = kMean(stream, clusters) // create K-mean model
    kmeanStream.print() // print k-mean results. It is pairs (k, m), where k - is a message text, m - is a cluster number to which message relates
    
    if (predictOutput != null) {
      kmeanStream.saveAsTextFiles(predictOutput) // save to results to the file, if file name specified
    }

    ssc.start() // run spark streaming application
    ssc.awaitTermination() // wait the end of the application
  }

  /**
  * transform stream of strings to stream of (string, vector) pairs and set this stream as input data for prediction
  */
  def kMean(dStream: DStream[String], clusters: KMeansModel): DStream[(String, Int)] = {
    dStream.map(s => (s, Utils.featurize(s))).map(p => (p._1, clusters.predict(p._2))) 
  }

}
