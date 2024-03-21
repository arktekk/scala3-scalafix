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

import fix.ImplicitConversions._
import scalafix.v1.{Patch, SemanticDocument, SemanticRule}

import scala.meta._
import scala.meta.tokens.Token

class ImplicitConversions extends SemanticRule("ImplicitConversions") {

  override def fix(implicit doc: SemanticDocument): Patch = {

    doc.tree.collect {

      case ImplicitDefWithFunctionArg(d, inType, outType) =>
        (for {
          pathDefToVal <- d.tokens.find(_.is[Token.KwDef]).map(Patch.replaceToken(_, "val"))
          eqSignToken <- d.tokens.find(_.is[Token.Equals])
          argTokens = d.tokens.dropWhile(_.isNot[Token.LeftParen]).dropRightWhile(_.isNot[Token.RightParen])
        } yield pathDefToVal +
          Patch.removeTokens(argTokens) +
          Patch.addRight(eqSignToken, s" ${argTokens.toString()} =>") +
          Patch.replaceTree(outType, conversionCode(inType, outType))).asPatch

      case ImplicitValWithFunctionType(ts) =>
        Patch.replaceTree(ts, conversionCode(ts.params, ts.res))

      case DefWithImplicitFunctionType(typeFunctions) =>
        typeFunctions.foldRight(Patch.empty) { case (tf, patches) =>
          patches + Patch.replaceTree(tf, conversionCode(tf.params, tf.res))
        }
    }.asPatch
  }

  private def conversionCode(inType: Type, retType: Type): String =
    conversionCode(inType :: Nil, retType)

  private def conversionCode(inType: List[Type], retType: Type): String = {
    val inStr = inType match {
      case one :: Nil => one.toString()
      case many       => many.map(_.toString()).mkString("(", ", ", ")")
    }
    s"Conversion[$inStr, $retType]"
  }
}

object ImplicitConversions {

  object ImplicitValWithFunctionType {
    def unapply(tree: Tree): Option[Type.Function] =
      tree match {
        case Defn.Val(mods, _, Some(ts: Type.Function), _) if mods.exists(_.is[Mod.Implicit]) => Some(ts)
        case _                                                                                => None
      }
  }
  object ImplicitDefWithFunctionArg {
    def unapply(tree: Tree): Option[(Defn.Def, Type, Type)] =
      tree match {
        case d @ Defn.Def(mods, Term.Name(_), _, (firstParam :: Nil) :: Nil, Some(outType: Type.Name), _)
            if mods.exists(_.is[Mod.Implicit]) && firstParam.mods.forall(_.isNot[Mod.Implicit]) =>
          firstParam.decltpe.map(inType => (d, inType, outType))
        case _ => None
      }
  }

  object DefWithImplicitFunctionType {
    def unapply(tree: Tree): Option[List[Type.Function]] = tree match {
      case Defn.Def(_, _, _, LastImplicitTypeFunctionParams(typeFunctions), _, _) => Some(typeFunctions)
      case _                                                                      => None
    }

    object LastImplicitTypeFunctionParams {
      def unapply(params: List[List[Term.Param]]): Option[List[Type.Function]] = {
        val lastParams = params.lastOption
        if (lastParams.exists(_.exists(_.mods.exists(a => a.is[Mod.Implicit] || a.is[Mod.Using])))) {
          val tfs = lastParams.toList
            .flatMap(_.map(_.decltpe))
            .collect { case Some(tf: Type.Function) => tf }
          if (tfs.isEmpty) None
          else Some(tfs)
        } else None
      }
    }
  }

}
