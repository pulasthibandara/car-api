name := "CarApi"
 
version := "1.0" 
      
lazy val `carapi` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
      
scalaVersion := "2.12.2"

libraryDependencies ++= Seq( ehcache , ws , guice, specs2 % Test )
libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "3.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "3.0.0",
  "com.github.tminglei" %% "slick-pg" % "0.16.1",
  "com.github.tminglei" %% "slick-pg_play-json" % "0.16.1"
)

libraryDependencies += "com.h2database" % "h2" % "1.4.197"

libraryDependencies += "org.sangria-graphql" %% "sangria" % "1.3.3"
libraryDependencies += "org.sangria-graphql" %% "sangria-relay" % "1.3.3"
libraryDependencies += "org.sangria-graphql" %% "sangria-play-json" % "1.0.4"

libraryDependencies ++= Seq(
  "com.mohiva" %% "play-silhouette" % "5.0.0",
  "com.mohiva" %% "play-silhouette-password-bcrypt" % "5.0.0",
  "com.mohiva" %% "play-silhouette-crypto-jca" % "5.0.0",
  "com.mohiva" %% "play-silhouette-persistence" % "5.0.0",
  "com.mohiva" %% "play-silhouette-testkit" % "5.0.0" % "test"
)

libraryDependencies += "net.codingwell" %% "scala-guice" % "4.1.1"
libraryDependencies += "com.iheart" %% "ficus" % "1.4.3"

javaOptions in Test += "-Dconfig.resource=application.test.conf"
