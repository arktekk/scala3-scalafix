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

import scalafix.lint.LintSeverity
import scalafix.v1.{Diagnostic, Patch, SemanticDocument, SemanticRule}
import scala.meta._

class GivenAndUsing extends SemanticRule("GivenAndUsing") {
  override def fix(implicit doc: SemanticDocument): Patch = {
    doc.tree.collect {
      case v: Defn.Val if v.mods.exists(_.is[Mod.Implicit]) =>
        if (v.decltpe.isDefined) replaceWithGiven(v, "val")
        else Patch.lint(GivenValWithoutDeclaredType(v))
      case d: Defn.Def =>
        List(
          if (d.mods.exists(m => m.is[Mod.Implicit]))
            if (onlyImplicitOrUsingParams(d)) replaceWithGiven(d, "def")
            else Patch.lint(GivenFunctionWithArgs(d))
          else Patch.empty,
          replaceWithUsing(d.paramss)
        ).asPatch
      case c: Defn.Class =>
        replaceWithUsing(c.ctor.paramss)
    }.asPatch
  }

  private def onlyImplicitOrUsingParams(d: Defn.Def): Boolean =
    d.paramss.forall(_.forall(_.mods.exists(m => m.is[Mod.Implicit] || m.is[Mod.Using])))

  private def replaceWithUsing(paramss: List[List[Term.Param]]) = {
    paramss.flatten
      .collectFirst {
        case p: Term.Param if p.mods.exists(_.is[Mod.Implicit]) =>
          p.mods
            .find(_.is[Mod.Implicit])
            .toList
            .flatMap(_.tokens)
            .headOption
            .map(t => Patch.replaceToken(t, "using"))
            .asPatch
      }
      .getOrElse(Patch.empty)
  }

  private def replaceWithGiven(v: Defn, replace: String): Patch = {
    val tokens = v.tokens
    val toModify = for {
      toReplace <- tokens.find(_.syntax == replace)
      toRemove <- tokens.findToken(_.syntax == "implicit").map(_.tokensWithTailingSpace())
    } yield Patch.removeTokens(toRemove) + Patch.replaceToken(toReplace, "given")
    toModify.getOrElse(Patch.empty)
  }

}

case class GivenValWithoutDeclaredType(value: Defn.Val) extends Diagnostic {
  override def severity: LintSeverity = LintSeverity.Warning

  override def message: String =
    s"The implicit val need to be typed to be rewritten to `given` syntax."

  override def position: _root_.scala.meta.Position = value.pos
}
case class GivenFunctionWithArgs(func: Defn.Def) extends Diagnostic {
  override def message: String =
    """Unable to rewrite to `given` syntax because we found a function with a non implicit argument.""".stripMargin

  override def position: Position = {
    val fromArgs = for {
      hPos <- func.paramss.headOption.flatMap(_.headOption).map(_.pos)
      lPos <- func.paramss.lastOption.flatMap(_.lastOption).map(_.pos)
    } yield
      if (hPos == lPos) hPos
      else Position.Range(hPos.input, hPos.startLine, hPos.startColumn, lPos.startLine, lPos.endColumn)
    fromArgs.getOrElse(func.pos)
  }

  override def severity: LintSeverity = LintSeverity.Warning
}
