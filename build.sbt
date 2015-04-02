import com.typesafe.sbt.less.Import.LessKeys
import de.johoop.testngplugin.TestNGPlugin
import de.johoop.jacoco4sbt.JacocoPlugin.jacoco

name := """jophiel"""

version := "0.1.0"

lazy val jophiel = (project in file("."))
                  .enablePlugins(PlayJava)
                  .disablePlugins(plugins.JUnitXmlReportPlugin)
                  .dependsOn(commons)
                  .aggregate(commons)

lazy val commons = RootProject(file("../judgels-play-commons"))

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaJdbc,
  javaWs,
  javaJpa.exclude("org.hibernate.javax.persistence", "hibernate-jpa-2" +
    ".0-api"),
  filters,
  cache,
  "org.hibernate" % "hibernate-entitymanager" % "4.3.7.Final",
  "commons-io" % "commons-io" % "2.4",
  "com.fasterxml.jackson.module" % "jackson-module-scala" % "2.0.2",
  "com.google.guava" % "guava" % "r05",
  "mysql" % "mysql-connector-java" % "5.1.26",
  "org.webjars" % "ckeditor" % "4.4.1",
  "com.typesafe.play" %% "play-mailer" % "2.4.0",
  "org.webjars" % "jquery-textcomplete" % "0.3.7"
)

TestNGPlugin.testNGSettings

TestNGPlugin.testNGSuites := Seq("testng.xml")

TestNGPlugin.testNGOutputDirectory := "target/testng"

jacoco.settings

parallelExecution in jacoco.Config := false

LessKeys.compress := true

LessKeys.optimization := 3

LessKeys.verbose := true

javaOptions in Test ++= Seq(
  "-Dconfig.resource=test.conf"
)

javacOptions ++= Seq("-s", "app")
