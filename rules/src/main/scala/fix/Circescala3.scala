package fix

import scalafix.v1._

import scala.meta._

class Circescala3 extends SemanticRule("Circescala3") {

  override def fix(implicit doc: SemanticDocument): Patch = {

    //    println("Tree.syntax: " + doc.tree.syntax)
    //    println("Tree.structure: " + doc.tree.structure)
    //    println("Tree.structureLabeled: " + doc.tree.structureLabeled)

    doc.tree.collect { case ClassWithCompanion(c, ImplicitDeriveEncoder(v)) =>
      (Patch.addRight(c, " derives Encoder.AsObject") +: v.tokens.map(Patch.removeToken)).asPatch
    }.asPatch
  }

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

object ImplicitDeriveEncoder {
  def unapply(o: Defn.Object): Option[Defn.Val] =
    o.templ.stats.collectFirst {
      case v @ Defn.Val(
            mods,
            _,
            Some(
              Type.Apply(
                Type.Select(Term.Name("Encoder"), Type.Name("AsObject")),
                (typeName: Type.Name) :: Nil
              )
            ),
            _
          ) if typeName.value == o.name.value && hasImplicitMod(mods) =>
        v
    }

  private def hasImplicitMod(mods: List[Mod]) =
    mods.exists {
      case _: Mod.Implicit => true
      case _               => false
    }
}
