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
    doc.tree.collect { case pkg@Pkg.Object(_, name, template) =>
      val newPkg = Pkg(name, List(Term.Block(template.stats)))
      val newTerm = Term.Name(name.value + "Impl")
      val newObject = q"private object $newTerm".copy(templ = template.copy(stats = Nil))
      Patch.addLeft(pkg, newPkg.syntax + "\n") + Patch.replaceTree(pkg, "\n" + newObject.syntax) + Patch.addRight(pkg, s"\nexport ${newTerm.value}.*")
    }.asPatch
  }
}
