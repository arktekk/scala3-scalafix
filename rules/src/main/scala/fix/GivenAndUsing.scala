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

import scalafix.v1.{Patch, SemanticDocument, SemanticRule}

import scala.meta.{Defn, Term}

class GivenAndUsing extends SemanticRule("GivenAndUsing") {
  override def fix(implicit doc: SemanticDocument): Patch = {
    doc.tree.collect {
      case v: Defn.Val if v.mods.exists(_.isModImplicit) =>
        replaceWithGiven(v, "val")
      case d: Defn.Def =>
        List(
          if (d.mods.exists(_.isModImplicit)) replaceWithGiven(d, "def") else Patch.empty,
          replaceWithUsing(d.paramss)
        ).asPatch
      case c: Defn.Class =>
        replaceWithUsing(c.ctor.paramss)
    }.asPatch
  }

  private def replaceWithUsing(paramss: List[List[Term.Param]]) = {
    paramss.flatten
      .collectFirst {
        case p: Term.Param if p.mods.exists(_.isModImplicit) =>
          p.mods
            .find(_.isModImplicit)
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
