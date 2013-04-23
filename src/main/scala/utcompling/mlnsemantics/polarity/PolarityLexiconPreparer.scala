package utcompling.mlnsemantics.polarity

import scala.io.Source

import utcompling.scalalogic.discourse.candc.boxer.expression.interpreter.impl._
import utcompling.scalalogic.discourse.candc.boxer.expression.BoxerExpression
import utcompling.scalalogic.discourse.candc.parse.output.impl._
import utcompling.scalalogic.discourse.impl.BoxerDiscourseInterpreter
import utcompling.scalalogic.discourse.impl.CandcDiscourseParser
import utcompling.scalalogic.discourse.DiscourseInterpreter
import utcompling.scalalogic.discourse.DiscourseParser
import dhg.util.FileUtil._
import utcompling.scalalogic.discourse._
import utcompling.scalalogic.discourse.impl._
import utcompling.scalalogic.discourse.candc.boxer._
import utcompling.scalalogic.discourse.candc.boxer.expression._
import utcompling.scalalogic.discourse.candc.boxer.expression.parse.BoxerExpressionParser
import utcompling.scalalogic.discourse.candc.boxer.expression.interpreter.impl._
import utcompling.scalalogic.drt.expression._
import utcompling.scalalogic.top.expression._
import utcompling.scalalogic.discourse.candc.call._
import utcompling.scalalogic.discourse.candc.call.impl._
import utcompling.scalalogic.discourse.candc.parse.output.impl._
import scala.collection.mutable.ListBuffer
import utcompling.mlnsemantics.datagen._
import utcompling.scalalogic.util.SeqUtils
import dhg.util.FileUtil
import utcompling.mlnsemantics.datagen.Tokenize
import dhg.util.CollectionUtil._
import dhg.util.CollectionUtil._

object PolarityLexiconPreparer {
  def main(args: Array[String]): Unit = {

    val factRe = """^fact_([pn])\*?$""".r
    val implRe = """^impl(_p([pn])\*?)?(_n([pn])\*?)?$""".r

    val pdi = new PrintingDiscourseInterpreter()

    writeUsing(File("resources/polarity-lexicon/polarity_lexicon_expanded.txt")) { f =>
      for (line <- File("resources/polarity-lexicon/polarity_lexicon.txt").readLines) {
        val (text, comment) =
          line.split("#", 2) match {
            case Array(text, comment) => (text, comment)
            case Array(text) => (text, "")
          }

        if (text.nonEmpty) {
          val Array(lemma, parcSubcat, signature, example) = line.split("\t")

          val (pos, requiredRelationsString, relation) =
            parcSubcat match {
              case "V-SUBJ-XCOMPinf" => ("VB", "xcomp+to", "theme")
              case "V-SUBJ-OBJ-XCOMPinf" => ("VB", "xcomp+to", "theme")
              case "V-SUBJ-OBJexpl-XCOMPinf" => ("VB", "xcomp", "theme")
              case "V-SUBJ-COMPEXthat" => ("VB", "ccomp", "theme")
              //case "V-SUBJ-OBJ-COMPEXthat" => ("VB", "ccomp", "theme")
              case "V-SUBJ-OBJ-COMPEXopt_extra" => ("VB", "ccomp+that", "theme")
              case "V-SUBJexpl-XCOMPinf" => ("VB", "xcomp+to", "theme")
              case "V-SUBJ-XCOMPinf_prt(on_)" => ("VB", "xcomp+to", "theme")
              case "V-SUBJ-OBLto-COMPEXthat" => ("VB", "ccomp+that", "theme")
              case _ =>
                println()
                println(example)
                println(parcSubcat)
                pdi.interpret(Tokenize(example).mkString(" ")); ("???", "???", "???")
            }

          f.write(List(lemma, parcSubcat, pos, requiredRelationsString, relation, signature, example, comment, "##automatically generated").mkString("\t").trim + "\n")

        }
        else {
          f.write(line + "\n")

        }
      }

    }
  }

  class PrintingDiscourseInterpreter(
    boxerDiscourseInterpreter: DiscourseInterpreter[DrtExpression] = new BoxerDiscourseInterpreter(
      new Boxer2DrtExpressionInterpreter(),
      CandcImpl.findBinary(Some(FileUtil.pathjoin(System.getenv("HOME"), "bin/candc/bin"))),
      BoxerImpl.findBinary(Some(FileUtil.pathjoin(System.getenv("HOME"), "bin/candc/bin")))),
    candcDiscourseParser: DiscourseParser[Discourse] = new CandcDiscourseParser(CandcImpl.findBinary(Some(FileUtil.pathjoin(System.getenv("HOME"), "bin/candc/bin")))))
    extends DiscourseInterpreter[Null] {

    override def batchInterpretMultisentence(inputs: List[List[String]], discourseIds: Option[List[String]] = None, question: Boolean = false, verbose: Boolean = false): List[Option[Null]] = {
      val newDiscourseIds = discourseIds.getOrElse((0 until inputs.length).map(_.toString).toList)
      val boxerResults = this.boxerDiscourseInterpreter.batchInterpretMultisentence(inputs, Some(newDiscourseIds), question, verbose)
      val parseResults = this.candcDiscourseParser.batchParseMultisentence(inputs, Map(), Some(newDiscourseIds), if (question) Some("question") else Some("boxer"), verbose)
      require(boxerResults.length == parseResults.length)
      require(boxerResults.length == 1)

      for ((Some(boxerResult), Some(parseResult)) <- boxerResults zipSafe parseResults) {
        val List(sentence) = parseResult.sentences
        sentence.words.foreach { w => println("%s %s".format(w, if (w.dependencies.nonEmpty) w.dependencies.mapValues(_.toList.sortBy(_.index).map(_.word)).mkString(", ") else "")) }
        boxerResult.pprint
      }

      return List(Some(null))
    }
  }

}
