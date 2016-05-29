package com.supergloo

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
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

    processStream(args, stream)

    ssc.start()
    ssc.awaitTermination()
  }

  def processStream(args: Array[String], stream: DStream[String]): Unit = {
    args match {
      case Array(_, _, path, _*) => stream.saveAsTextFiles(args(2))
      case _ => return
    }


  }

}