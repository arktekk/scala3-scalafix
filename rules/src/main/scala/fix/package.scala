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

import scala.meta.{Defn, Mod}
import scalafix.XtensionScalafixProductInspect

package object fix {

  implicit class ModExt(m: Mod) {
    def isModImplicit: Boolean =
      m match {
        case _: Mod.Implicit => true
        case _               => false
      }

    def isModCase: Boolean =
      m match {
        case _: Mod.Case => true
        case _           => false
      }
  }

  def printDefn(t: Defn): Unit = {
    println(t.structureLabeled)
  }

}
