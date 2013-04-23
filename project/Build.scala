import sbt._
import Keys._

object MlnSemanticsBuild extends Build {

  lazy val main = Project("mln-semantics", file(".")) dependsOn(scalaLogic)

  lazy val scalaLogic = Project("scala-logic", file("scala-logic")) dependsOn(scalautil)

  lazy val scalautil = Project("scala-util", file("scala-logic/scala-util"))

}

