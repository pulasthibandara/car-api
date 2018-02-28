name := "CarApi"
 
version := "1.0" 
      
lazy val `carapi` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
      
scalaVersion := "2.12.2"

libraryDependencies ++= Seq( ehcache , ws , specs2 % Test , guice )
libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "3.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "3.0.0"
)

libraryDependencies += "com.h2database" % "h2" % "1.4.196"

libraryDependencies += "org.sangria-graphql" %% "sangria" % "1.3.3"
libraryDependencies += "org.sangria-graphql" %% "sangria-relay" % "1.3.3"
libraryDependencies += "org.sangria-graphql" %% "sangria-play-json" % "1.0.4"


unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

      