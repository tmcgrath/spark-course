name := "spark-sql-examples"
 
version := "1.0"

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

scalaVersion := "2.11.8"
 
resolvers += "jitpack" at "https://jitpack.io"
 
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-sql" % "1.6.1" % "provided",
  "com.databricks" %% "spark-csv_2.11" % "1.3.0",
  "mysql" % "mysql-connector-java" % "5.1.12"
)
