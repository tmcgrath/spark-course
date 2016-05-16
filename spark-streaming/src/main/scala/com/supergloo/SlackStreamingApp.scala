package com.supergloo
 
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
 
/**
  * Spark Streaming Example App
  */
object SlackStreamingApp {
 
  def main(args: Array[String]) {
    val conf = new SparkConf().setMaster(args(0)).setAppName("SlackStreaming")
    val ssc = new StreamingContext(conf, Seconds(5))
    val stream = ssc.receiverStream(new SlackReceiver(args(1)))
    stream.print()
    if (args.length > 2) {
      stream.saveAsTextFiles(args(2))
    }
    ssc.start()
    ssc.awaitTermination()
  }
 
}