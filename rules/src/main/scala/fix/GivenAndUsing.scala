/*
 * Copyright 2022 Arktekk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fix

import fix.matchers.{ApplyImplicitArgs, ImplicitOrUsingMod}
import scalafix.lint.LintSeverity
import scalafix.v1._

import scala.meta._

class GivenAndUsing extends SemanticRule("GivenAndUsing") {

  override def fix(implicit doc: SemanticDocument): Patch = {
    val givenAndUsingPass = doc.tree.collect[List[APatch]] {
      case v: Defn.Val if v.mods.exists(_.is[Mod.Implicit]) =>
        if (v.decltpe.isDefined) replaceWithGiven(v, "val") :: Nil
        else APatch.Lint(GivenValWithoutDeclaredType(v)) :: Nil
      case d: Defn.Def =>
        List(
          if (d.mods.exists(m => m.is[Mod.Implicit]))
            if (onlyImplicitOrUsingParams(d)) replaceWithGiven(d, "def")
            else APatch.Lint(GivenFunctionWithArgs(d))
          else APatch.Empty
        ) ++ replaceWithUsing(d.paramClauseGroups.flatMap(_.paramClauses).map(_.values))
      case c: Defn.Class =>
        replaceWithUsing(c.ctor.paramClauses.map(_.values).toList)
    }

    val givenImports = givenAndUsingPass.flatMap(_.collect { case APatch.Given(_, symbol) => symbol.owner }).toSet
    val usingRefs = givenAndUsingPass.flatMap(_.collect { case APatch.Using(_, symbol) => symbol }).toSet

    val importAndUsingPass = doc.tree.collect {
      case i @ Importer(_, importees)
          if importees.exists(_.is[Importee.Wildcard]) && givenImports.contains(i.ref.symbol) =>
        if (importees.length == 1) Patch.addAround(importees.head, "{ given, ", " }")
        else if (importees.exists(_.is[Importee.GivenAll])) Patch.empty
        else Patch.addLeft(importees.head, "given")

      case ApplyImplicitArgs(symbol, args) if usingRefs.contains(symbol) && !args.mod.exists(_.is[Mod.Using]) =>
        args.headOption.map(h => Patch.addLeft(h, "using ")).getOrElse(Patch.empty)

    }.asPatch

    givenAndUsingPass.flatMap(_.map(_.patch)).asPatch + importAndUsingPass
  }

  private def onlyImplicitOrUsingParams(d: Defn.Def): Boolean =
    d.paramClauseGroups.forall(_.paramClauses.forall(_.mod.exists {
      case ImplicitOrUsingMod(_) => true
      case _                     => false
    }))

  private def replaceWithUsing(paramss: List[List[Term.Param]])(implicit doc: SemanticDocument): List[APatch] = {
    val usingPatch = paramss.reverse.flatten
      .collectFirst { case p @ ImplicitOrUsingMod(mod) =>
        if (mod.is[Mod.Using])
          APatch.Empty
        else {
          val pp = mod.tokens.headOption
            .map(t => Patch.replaceToken(t, "using"))
            .asPatch
          APatch.Using(pp, p.symbol.owner)
        }
      }
    val lintPatchers = paramss.flatMap(_.collectFirst { case ImplicitOrUsingMod(mod) => mod }.toList) match {
      case Nil      => List.empty
      case _ :: Nil => List.empty
      case other =>
        val crateLintOf = if (other.exists(_.is[Mod.Using])) other.filter(_.isNot[Mod.Using]) else other.dropRight(1)
        crateLintOf.map(mod => APatch.Lint(MultipleImplicits(mod.pos)))
    }
    lintPatchers ++ usingPatch
  }

  private def replaceWithGiven(v: Defn, replace: String)(implicit doc: SemanticDocument): APatch = {
    val tokens = v.tokens
    val toModify = for {
      toReplace <- tokens.find(_.syntax == replace)
      toRemove <- tokens.findToken(_.syntax == "implicit").map(_.tokensWithTailingSpace())
    } yield APatch.Given(Patch.removeTokens(toRemove) + Patch.replaceToken(toReplace, "given"), v.symbol)
    toModify.getOrElse(APatch.Empty)
  }

}

sealed trait APatch {
  def patch: Patch
}
object APatch {
  case object Empty extends APatch { val patch: Patch = Patch.empty }
  case class Lint(diagnostic: Diagnostic) extends APatch { def patch: Patch = Patch.lint(diagnostic) }
  case class Using(patch: Patch, symbol: Symbol) extends APatch
  case class Given(patch: Patch, symbol: Symbol) extends APatch
}

case class GivenValWithoutDeclaredType(value: Defn.Val) extends Diagnostic {
  override def severity: LintSeverity = LintSeverity.Warning

  override def message: String =
    s"The implicit val need to be typed to be rewritten to `given` syntax."

  override def position: _root_.scala.meta.Position = value.pos
}

case class MultipleImplicits(pos: _root_.scala.meta.Position) extends Diagnostic {
  override def severity: LintSeverity = LintSeverity.Warning

  override def message: String =
    "Not allowed to use `using` because it's defined in the next argument block."

  override def position: _root_.scala.meta.Position = pos
}

case class GivenFunctionWithArgs(func: Defn.Def) extends Diagnostic {
  override def message: String =
    """Unable to rewrite to `given` syntax because we found a function with a non implicit argument.""".stripMargin

  override def position: Position = {
    val paramss = func.paramClauseGroups.flatMap(_.paramClauses).flatMap(_.values)
    val fromArgs = for {
      hPos <- paramss.headOption.map(_.pos)
      lPos <- paramss.lastOption.map(_.pos)
    } yield
      if (hPos == lPos) hPos
      else Position.Range(hPos.input, hPos.startLine, hPos.startColumn, lPos.startLine, lPos.endColumn)
    fromArgs.getOrElse(func.pos)
  }

  override def severity: LintSeverity = LintSeverity.Warning
}
