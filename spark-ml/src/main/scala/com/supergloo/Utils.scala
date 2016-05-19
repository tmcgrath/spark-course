package com.supergloo

import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.linalg.Vector

/**
  * Apache Spark with Scala course
  */
object Utils {

  val NUM_DEMENSIONS: Int = 1000

  val tf = new HashingTF(NUM_DEMENSIONS)

  /**
   * This uses min hash algorithm https://en.wikipedia.org/wiki/MinHash to transform 
   * string to vector of double, which is required for k-means
   */
  def featurize(s: String): Vector = {
    tf.transform(s.sliding(2).toSeq)
  }

}
