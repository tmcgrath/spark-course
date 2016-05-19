package com.supergloo

import com.beust.jcommander.{JCommander, Parameter}
import org.apache.spark.mllib.clustering.KMeansModel
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Apache Spark with Scala course
  */
object SlackMLApp {

  object Config {
    @Parameter(names = Array("-st", "--slackToken"))
    var slackToken: String = null
    @Parameter(names = Array("-nc", "--numClusters"))
    var numClusters: Int = 4
    @Parameter(names = Array("-po", "--predictOutput"))
    var predictOutput: String = null
    @Parameter(names = Array("-td", "--trainData"))
    var trainData: String = null
    @Parameter(names = Array("-ml", "--modelLocation"))
    var modelLocation: String = null
  }

  def main(args: Array[String]) {
    new JCommander(Config, args.toArray: _*)
    val conf = new SparkConf().setAppName("SlackStreamingWithML")
    val sparkContext = new SparkContext(conf)
    
    // optain existing or create new model
    val clusters: KMeansModel =
      if (Config.trainData != null) {
        KMeanTrainTask.train(sparkContext, Config.trainData, Config.numClusters, Config.modelLocation)
      } else {
        if (Config.modelLocation != null) {
          new KMeansModel(sparkContext.objectFile[Vector](Config.modelLocation).collect())
        } else {
          throw new IllegalArgumentException("Either modelLocation or trainData should be specified")
        }
      }

    if (Config.slackToken != null) {
      SlackStreamingTask.run(sparkContext, Config.slackToken, clusters, Config.predictOutput)
    }
    
  }

}
