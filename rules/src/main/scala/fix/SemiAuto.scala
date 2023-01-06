/*
 * Copyright 2023 Arktekk
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

import metaconfig.Configured
import scalafix.lint.LintSeverity
import scalafix.v1.{Patch, _}

import scala.meta._

class SemiAuto(semiAutoConfig: SemiAutoConfig) extends SemanticRule("SemiAuto") {
  def this() = this(SemiAutoConfig())

  override def withConfiguration(configuration: Configuration): Configured[Rule] =
    configuration.conf
      .getOrElse("SemiAuto")(this.semiAutoConfig)
      .map(newConfig => new SemiAuto(newConfig))

  override def fix(implicit doc: SemanticDocument): Patch = {
    val config = semiAutoConfig.allRewrites.map(c => c.typeClass -> c).toMap

    doc.tree.collect { case CaseClassWithCompanion(caseClass, companion @ SemiAutoDerived(items)) =>
      val lint = items
        .filterNot(c => config.contains(c.deriveType))
        .map(item => Patch.lint(DerivesCandidate(item.defn.pos, item)))
        .asPatch

      val applied = items.flatMap(item => config.get(item.deriveType).map(item -> _)) match {
        case Nil => Patch.empty
        case toRewrite =>
          val derivePos =
            caseClass.templ.derives.lastOption
              .orElse(caseClass.templ.inits.lastOption)
              .orElse(if (caseClass.templ.stats.nonEmpty) Some(caseClass.ctor) else None)
              .getOrElse(caseClass)
          val base = if (caseClass.templ.derives.isEmpty) " derives " else ", "
          val derivePatch = Patch.addRight(derivePos, base ++ toRewrite.map(_._2.derived).mkString(", "))
          val removePatch =
            if (childrenInCompanion(companion) == toRewrite.size)
              Patch.removeTokens(companion.tokens.tokensWithLeadingSpace())
            else Patch.removeTokens(toRewrite.flatMap(_._1.defn.tokens.tokensWithLeadingSpace()))

          derivePatch + removePatch
      }
      lint + applied
    }.asPatch
  }

  private def childrenInCompanion(companion: Defn.Object): Int = {
    companion.templ.children.count {
      case Self(Name.Anonymous(), None) => false
      case _                            => true
    }
  }
}

object CaseClassWithCompanion {
  def unapply(t: Tree): Option[(Defn.Class, Defn.Object)] =
    t match {
      case c @ Defn.Class(mods, cName, _, _, _) if mods.exists(_.is[Mod.Case]) =>
        c.parent.flatMap { st =>
          st.children.collectFirst {
            case o @ Defn.Object(_, oName, _) if cName.value == oName.value => c -> o
          }
        }
      case _ => None
    }

}

case class SemiAutoDerived(deriveType: String, defn: Defn)

object SemiAutoDerived {

  def unapply(o: Defn.Object)(implicit doc: SemanticDocument): Option[List[SemiAutoDerived]] =
    nonEmptyList(o.templ.stats.collect {
      case g @ Defn.GivenAlias(_, _, _, _, typeApply @ Type.Apply(_, (typeName: Type.Name) :: Nil), body)
          if matchingType(o, typeName) && isSemiAuto(body) =>
        SemiAutoDerived(typeApply.symbol.normalized.value.dropRight(1), g)
      case v @ Defn.Val(mods, _, Some(applied @ Type.Apply(_, (typeName: Type.Name) :: Nil)), body)
          if matchingType(o, typeName) && mods.exists(_.is[Mod.Implicit]) && isSemiAuto(body) =>
        SemiAutoDerived(findSymbolFromSignature(v).getOrElse(applied.symbol).normalized.value.dropRight(1), v)
      case v @ Defn.Def(mods, _, _, _, Some(applied @ Type.Apply(_, (typeName: Type.Name) :: Nil)), body)
          if matchingType(o, typeName) && mods.exists(_.is[Mod.Implicit]) && isSemiAuto(body) =>
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

case class DerivesCandidate(position: Position, derived: SemiAutoDerived) extends Diagnostic {
  private def extractType(t: Type): String =
    t match {
      case Type.Apply(typ, _) => typ.syntax
      case t                  => t.syntax
    }

  private def toType: String =
    (derived.defn match {
      case v: Defn.Val        => v.decltpe.map(extractType)
      case g: Defn.GivenAlias => Some(extractType(g.decltpe))
      case _                  => None
    }).getOrElse("")

  def message =
    s"""Can be used in derives clause. Add to SemiAuto.rewrites config:
       |{ typeclass = "${derived.deriveType}", derived = "$toType" }
       |
       |""".stripMargin

  override def severity: LintSeverity = LintSeverity.Info
}
