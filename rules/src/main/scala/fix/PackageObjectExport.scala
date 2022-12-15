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

import scalafix.v1._
import scala.meta._

class PackageObjectExport extends SemanticRule("PackageObjectExport") {

  override def fix(implicit doc: SemanticDocument): Patch = {
    doc.tree.collect { case pkg @ Pkg.Object(_, name, _) =>
      val tokens = pkg.tokens
      val maybeSplit = tokens.splitOn(t => t.is[Token.Colon] || t.is[Token.LeftBrace])
      val newTerm = name.value + "Impl"

      maybeSplit match {
        case Some((objectTokens, restTokens)) =>
          val isNewSyntax = restTokens.head.is[Token.Colon]
          // should be configurable
          val indent = if (isNewSyntax) "  " else ""
          val maybeExtends = objectTokens.splitOn(t => t.is[Token.KwExtends])
          val extendsObject = maybeExtends match {
            case Some((_, extendsTokens)) =>
              val privatePackage =
                s"""|${indent}private object ${newTerm} ${extendsTokens.syntax.trim}
                    |${indent}export ${newTerm}.*
                    |""".stripMargin
              Patch.addRight(restTokens.last, "\n\n" + privatePackage)
            case None => Patch.empty
          }
          val packagePatch =
            Patch.removeTokens(objectTokens) + Patch.addLeft(objectTokens.head, s"package ${name.value} ")
          packagePatch + extendsObject
        case None => Patch.empty
      }

    }.asPatch
  }
}
