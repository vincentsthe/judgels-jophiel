import de.johoop.testngplugin.TestNGPlugin
import de.johoop.jacoco4sbt.JacocoPlugin.jacoco

lazy val jophiel = (project in file("."))
    .enablePlugins(PlayJava, SbtWeb)
    .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)
    .dependsOn(playcommons)
    .aggregate(playcommons)
    .settings(
        name := "jophiel",
        version := "0.2.1",
        scalaVersion := "2.11.1",
        libraryDependencies ++= Seq(
            "org.webjars" % "jquery-textcomplete" % "0.3.7",
            "com.typesafe.play" %% "play-mailer" % "2.4.0"
        )
    )
    .settings(TestNGPlugin.testNGSettings: _*)
    .settings(
        aggregate in test := false,
        aggregate in jacoco.cover := false,
        TestNGPlugin.testNGSuites := Seq("test/resources/testng.xml")
    )
    .settings(jacoco.settings: _*)
    .settings(
        parallelExecution in jacoco.Config := false
    )
    .settings(
        LessKeys.compress := true,
        LessKeys.optimization := 3,
        LessKeys.verbose := true
    )

lazy val playcommons = RootProject(file("../judgels-play-commons"))
