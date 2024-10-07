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

package fix.matchers

import scalafix.v1.SemanticDocument

import scala.meta._
import scalafix.v1._

case class SemiAutoDerived(
    deriveType: String,
    defn: Defn
)

object SemiAutoDerived {

  def unapply(o: Defn.Object)(implicit doc: SemanticDocument): Option[List[SemiAutoDerived]] =
    nonEmptyList(o.templ.body.stats.collect {
      case g @ Defn.GivenAlias.After_4_6_0(
            _,
            _,
            _,
            typeApply @ Type.Apply.After_4_6_0(_, Type.ArgClause((typeName: Type.Name) :: Nil)),
            body
          ) if matchingType(o, typeName) && isSemiAuto(body) =>
        SemiAutoDerived(typeApply.symbol.normalized.value.dropRight(1), g)
      case v @ Defn
            .Val(mods, _, Some(applied @ Type.Apply.After_4_6_0(_, Type.ArgClause((typeName: Type.Name) :: Nil))), body)
          if matchingType(o, typeName) && mods.exists(_.is[Mod.Implicit]) && isSemiAuto(body) =>
        SemiAutoDerived(findSymbolFromSignature(v).getOrElse(applied.symbol).normalized.value.dropRight(1), v)
      case v @ Defn.Def.After_4_6_0(
            mods,
            _,
            _,
            Some(applied @ Type.Apply.After_4_6_0(_, Type.ArgClause((typeName: Type.Name) :: Nil))),
            body
          ) if matchingType(o, typeName) && mods.exists(_.is[Mod.Implicit]) && isSemiAuto(body) =>
        SemiAutoDerived(findSymbolFromSignature(v).getOrElse(applied.symbol).normalized.value.dropRight(1), v)
    })

  private def matchingType(o: Defn.Object, typeName: Type.Name): Boolean =
    typeName.value == o.name.value

  private def isSemiAuto(t: Term)(implicit doc: SemanticDocument) = {
    t.symbol.normalized.value.contains(".semiauto.") || t.symbol.normalized.value.contains("derived")
  }

  private def nonEmptyList[A](l: List[A]): Option[List[A]] =
    if (l.isEmpty) None else Some(l)

  private def findSymbolFromSignature(defn: Defn)(implicit doc: SemanticDocument) = {
    val sym = defn.symbol
    sym.info.map(_.signature).flatMap {
      case ValueSignature(TypeRef(_, symbol, _)) =>
        Some(symbol)
      case _ => None
    }
  }

}
