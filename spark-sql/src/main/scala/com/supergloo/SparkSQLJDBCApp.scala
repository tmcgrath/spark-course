package com.supergloo
 
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.functions._
 
/**
  * Spark SQL with JDBC Example 
  */
object SparkSQLJDBCApp {
 
  def main(args: Array[String]) {

    val conf = new SparkConf().setAppName("SparkSQLJDBCApp")
    val spark = new SparkContext(conf)
    val sqlContext = new SQLContext(spark)

    val dataframe_mysql = sqlContext.read.format("jdbc").
                            option("url", "jdbc:mysql://localhost/sparksql").
                            option("driver", "com.mysql.jdbc.Driver").
                            option("dbtable", "baby_names").
                            option("user", "root").option("password", "root").load()

    dataframe_mysql.registerTempTable("names")

    println("Result of query:")
    dataframe_mysql.sqlContext.sql("select * from names").collect.foreach(println)


    spark.stop()
  }
 
}