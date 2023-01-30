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

import scala.annotation.tailrec
import scala.meta._
import scalafix.v1._

object ApplyImplicitArgs {

  @tailrec
  private def applyTermChain(term: Term, args: List[List[Term]]): List[List[Term]] = {
    term match {
      case t: Term.Apply => applyTermChain(t.fun, t.args :: args)
      case _             => args
    }
  }

  def unapply(tree: Tree)(implicit doc: SemanticDocument): Option[(Symbol, List[Term])] =
    tree match {
      case term: Term.Apply =>
        term.symbol.info
          .map(_.signature)
          .collect {
            case m: MethodSignature =>
              m.parameterLists
            case c: ClassSignature =>
              c.declarations
                .map(_.signature)
                .collect { case m: MethodSignature => m.parameterLists }
                .flatten
          }
          .filter(_.lastOption.exists(_.headOption.exists(_.symbol.info.exists(_.isImplicit))))
          .flatMap { params =>
            val argsList = applyTermChain(term, List.empty)
            if (params.length == argsList.length) {
              for {
                sym <- params.lastOption.flatMap(_.headOption.map(_.symbol.owner))
                arg <- argsList.lastOption
              } yield sym -> arg
            } else None
          }
      case _ => None
    }
}
