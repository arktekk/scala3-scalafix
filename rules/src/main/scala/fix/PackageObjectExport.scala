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
      val replaceName = tokens
        .findToken(_.text == name.value)
        .map(t => Patch.replaceToken(t.head, s"`${t.head.text}`"))
        .getOrElse(Patch.empty)
      val packagePatch = tokens.findToken(_.is[Token.KwPackage]) match {
        case Some(pkgToken) =>
          Patch.addLeft(pkg, s"package $name\n") + Patch.replaceToken(pkgToken.head, "private") + replaceName
        case None => Patch.empty
      }
      packagePatch + Patch.addRight(pkg, s"\nexport `${name.value}`.*")
    }.asPatch
  }
}
