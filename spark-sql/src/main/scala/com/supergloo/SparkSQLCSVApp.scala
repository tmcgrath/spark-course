package com.supergloo
 
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.functions._
 
/**
  * Spark SQL with JDBC Example 
  */
object SparkSQLCSVApp {
 
  def main(args: Array[String]) {

    val conf = new SparkConf().setAppName("SparkCSVJDBCApp")
    val spark = new SparkContext(conf)
    val sqlContext = new SQLContext(spark)

    val baby_names = sqlContext.read.format("com.databricks.spark.csv").
                        option("header", "true").
                        option("inferSchema", "true").
                        load("baby_names.csv")

    baby_names.registerTempTable("names")

    println("Result of CSV query:")
    baby_names.sqlContext.sql("select distinct Year from names").collect.foreach(println)


    spark.stop()
  }
 
}