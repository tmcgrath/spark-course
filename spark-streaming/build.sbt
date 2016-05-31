name := "spark-streaming-example"
 
version := "1.0"

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

excludedJars in assembly <<= (fullClasspath in assembly) map { cp => 
  cp filter { 
		i => i.data.getName == "slf4j-api-1.7.12.jar" 
	    }
}

addCommandAlias("sanity", ";clean ;compile ;scalastyle ;coverage ;test; coverageReport")

scalaVersion := "2.11.8"
 
resolvers += "jitpack" at "https://jitpack.io"
 
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-streaming" % "1.6.1" % "provided",
// comment above line and uncomment the following to run in sbt
// "org.apache.spark" %% "spark-streaming" % "1.6.1",
  "org.scalaj" %% "scalaj-http" % "2.3.0",
  "org.jfarcand" % "wcs" % "1.5",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test"
)
