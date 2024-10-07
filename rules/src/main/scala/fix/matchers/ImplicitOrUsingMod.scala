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

import scala.meta._

object ImplicitOrUsingMod {
  def unapply(mod: Mod): Option[Mod] =
    if (mod.is[Mod.Implicit] || mod.is[Mod.Using]) Some(mod) else None

  def unapply(param: Term.Param): Option[Mod] =
    param.mods.collectFirst { case ImplicitOrUsingMod(mod) => mod }
}
