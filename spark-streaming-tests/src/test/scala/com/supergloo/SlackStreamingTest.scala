package com.supergloo

import com.supergloo.SlackStreamingApp._
import org.apache.hadoop.mapred.InvalidInputException
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{ClockWrapper, Seconds, StreamingContext}
import org.scalatest.concurrent.Eventually
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

import scala.collection.mutable
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.reflect.io.Path
import scala.util.Try

class SlackStreamingTest extends FlatSpec with Matchers with Eventually with BeforeAndAfter {

  private val master = "local[1]"
  private val appName = "spark-streaming-test"
  private val filePath: String = "target/testfile"

  private var ssc: StreamingContext = _

  private val batchDuration = Seconds(1)

  var clock: ClockWrapper = _

  before {
    val conf = new SparkConf()
      .setMaster(master).setAppName(appName)
      .set("spark.streaming.clock", "org.apache.spark.streaming.util.ManualClock")

    ssc = new StreamingContext(conf, batchDuration)
    clock = new ClockWrapper(ssc)
  }

  after {
    if (ssc != null) {
      ssc.stop()
    }
    Try(Path(filePath + "-1000").deleteRecursively)
  }

  "Slack Streaming App " should " store streams into a file" in {
    val lines = mutable.Queue[RDD[String]]()
    val dstream = ssc.queueStream(lines)

    dstream.print()
    processStream(Array("", "", filePath), dstream)


    ssc.start()

    lines += ssc.sparkContext.makeRDD(Seq("b", "c"))
    clock.advance(1000)

    eventually(timeout(2 seconds)){
      val wFile: RDD[String] = ssc.sparkContext.textFile(filePath+ "-1000")
      wFile.count() should be (2)
      wFile.collect().foreach(println)
    }

  }

  "Slack Streaming App " should " store empty streams if no data received" in {
    val lines = mutable.Queue[RDD[String]]()
    val dstream = ssc.queueStream(lines)

    dstream.print()
    processStream(Array("", "", filePath), dstream)


    ssc.start()

    clock.advance(1000)

    eventually(timeout(1 seconds)){
      val wFile: RDD[String] = ssc.sparkContext.textFile(filePath+ "-1000")
      wFile.count() should be (0)
      wFile.collect().foreach(println)
    }

  }

  "Slack Streaming App " should " not store streams if argument is not passed" in {
    val lines = mutable.Queue[RDD[String]]()
    val dstream = ssc.queueStream(lines)

    dstream.print()
    processStream(Array("", ""), dstream)

    val wFile: RDD[String] = ssc.sparkContext.textFile(filePath+ "-1000")

    ssc.start()

    lines += ssc.sparkContext.makeRDD(Seq("b", "c"))
    clock.advance(2000)

    eventually(timeout(3 seconds)){
      a [InvalidInputException] should be thrownBy {
        wFile.count() should be (0)
      }
    }
  }}

