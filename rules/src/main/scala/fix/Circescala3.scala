package fix

import scalafix.v1.{Patch, _}

import scala.meta._

class Circescala3 extends SemanticRule("Circescala3") {

  override def fix(implicit doc: SemanticDocument): Patch = {

    //    println("Tree.syntax: " + doc.tree.syntax)
    //    println("Tree.structure: " + doc.tree.structure)
    //    println("Tree.structureLabeled: " + doc.tree.structureLabeled)

    val config = Set(
      Circescala3.Config("io.circe.Encoder.AsObject", "Encoder.AsObject"),
      Circescala3.Config("io.circe.Encoder", "Encoder.AsObject"),
      Circescala3.Config("io.circe.Decoder", "Decoder"),
      Circescala3.Config("io.circe.Codec.AsObject", "Codec.AsObject"),
      Circescala3.Config("io.circe.Codec", "Codec.AsObject")
    ).map(c => c.typ -> c).toMap

    val implicitTypesToInclude = config.keySet
    doc.tree.collect { case ClassWithCompanion(c, ImplicitDeriveValues(items)) =>
      items.filter(item => implicitTypesToInclude.contains(item._1)) match {
        case Nil => Patch.empty
        case toRewrite =>
          val base = if (c.templ. derives.isEmpty) " derives " else ", "
          val derivePatch = Patch.addRight(c, base ++ toRewrite.map(_._1).map(config(_).derived).mkString(", "))
          val removeImplicitPatches = Patch.removeTokens(toRewrite.flatMap(_._2.tokens))
          derivePatch + removeImplicitPatches
      }
    }.asPatch
  }

}

object Circescala3 {

  case class Config(
      typ: String,
      derived: String
  )
}

object ClassWithCompanion {
  def unapply(t: Tree): Option[(Defn.Class, Defn.Object)] =
    t match {
      case c @ Defn.Class(_, cName, _, _, _) =>
        c.parent.flatMap { st =>
          st.children.collectFirst {
            case o @ Defn.Object(_, oName, _) if cName.value == oName.value => c -> o
          }
        }
      case _ => None
    }
}

object ImplicitDeriveValues {
  def unapply(o: Defn.Object)(implicit doc: SemanticDocument): Option[List[(String, Defn.Val)]] = {
    val vals = o.templ.stats.collect {
      case v @ Defn.Val(mods, _, Some(typeApply @ Type.Apply(_, (typeName: Type.Name) :: Nil)), _)
          if typeName.value == o.name.value && hasImplicitMod(mods) =>
        typeApply.symbol.normalized.value.dropRight(1) -> v
    }
    if (vals.isEmpty) None
    else Some(vals)
  }

  private def hasImplicitMod(mods: List[Mod]) =
    mods.exists {
      case _: Mod.Implicit => true
      case _               => false
    }
}
