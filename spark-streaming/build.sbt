name := "spark-streaming-example"
 
version := "1.0"

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

excludedJars in assembly <<= (fullClasspath in assembly) map { cp => 
  cp filter { i => i.data.getName == "slf4j-api-1.7.12.jar" 
           //   || i.data.getName == "async-http-client-1.9.28.jar" 
	    }
}
 
scalaVersion := "2.11.8"
 
resolvers += "jitpack" at "https://jitpack.io"
 
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-streaming" % "1.6.1" % "provided",
//  "org.apache.spark" %% "spark-streaming" % "1.6.1",
  "org.scalaj" %% "scalaj-http" % "2.3.0",
  "org.jfarcand" % "wcs" % "1.5" 
)
