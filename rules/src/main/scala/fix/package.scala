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

import scala.annotation.tailrec
import scala.meta._
import scalafix.XtensionScalafixProductInspect

package object fix {

  def printDefn(t: Defn): Unit = {
    println(t.structureLabeled)
  }

  implicit class TokensExt(tokens: Tokens) {

    def findToken(p: Token => Boolean): Option[Tokens] = {
      val newTokens = tokens.dropWhile(!p(_)).take(1)
      if (newTokens.length == 1) Some(newTokens)
      else None
    }

    def splitOn(p: Token => Boolean): Option[(Tokens, Tokens)] = {
      val idx = tokens.indexWhere(p)
      if (idx == -1) None else Some(tokens.splitAt(idx))
    }

    def tokensWithTailingSpace(): List[Token] = {
      @tailrec
      def run(pos: Int, ws: List[Token]): List[Token] = {
        val next = pos + 1
        if (next < tokens.tokens.length && tokens.tokens(next).is[Token.Space]) run(next, tokens.tokens(next) :: ws)
        else ws
      }

      tokens.toList ++ run(tokens.start, Nil)
    }

    def tokensWithLeadingSpace(): List[Token] = {
      @tailrec
      def run(pos: Int, ws: List[Token]): List[Token] = {
        val next = pos - 1
        if (next >= 0 && tokens.tokens(next).is[Token.Space]) run(next, tokens.tokens(next) :: ws)
        else ws
      }

      run(tokens.start, Nil) ++ tokens.toList
    }
  }

}
