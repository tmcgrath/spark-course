package com.supergloo

import java.io._
import org.apache.spark.rdd.RDD
import org.apache.spark.SparkContext
import org.apache.spark.mllib.clustering.{KMeans, KMeansModel}

import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.feature.IDF
import org.apache.spark.mllib.linalg.Vectors

/**
  * Apache Spark with Scala course
  */
object KMeanTrainTask {

  val numIterations = 20

  def train(sparkContext: SparkContext, trainData: String, numClusters: Int, modelLocation: String): KMeansModel = {
    
    if (new File(modelLocation).exists) removePrevious(modelLocation)

    val trainRdd = sparkContext.textFile(trainData)

    val parsedData = trainRdd.map(Utils.featurize).cache()
    
    val model = KMeans.train(parsedData, numClusters, numIterations)

    sparkContext.makeRDD(model.clusterCenters, numClusters).saveAsObjectFile(modelLocation)
    
    val example = trainRdd.sample(withReplacement = false, 0.1).map(s => (s, model.predict(Utils.featurize(s)))).collect()
    println("Prediction examples:")
    example.foreach(println)

    model
  }

  def removePrevious(path: String) = {
    def getRecursively(f: File): Seq[File] = 
      f.listFiles.filter(_.isDirectory).flatMap(getRecursively) ++ f.listFiles
    getRecursively(new File(path)).foreach{f => 
      if (!f.delete()) 
        throw new RuntimeException("Failed to delete " + f.getAbsolutePath)
    }
    new File(path).delete()
    Thread.sleep(2000)
  }
}