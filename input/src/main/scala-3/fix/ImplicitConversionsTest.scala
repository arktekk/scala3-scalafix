/*
rule = ImplicitConversions
 */

package fix

import scala.language.implicitConversions

// format: off
object ImplicitConversionsTest {
  implicit def implicitDefWithFunctionArg1(x: Int): String = x.toString
  implicit def implicitDefWithFunctionArg2(x: (Int, Long)): String = x.toString

  implicit val implicitValWithFunctionType1: Long => String = _.toString
  implicit val implicitValWithFunctionType2: (Int, Long) => String = (i, l ) => i.toString

  def defWithImplicitFunctionType1(x: Int)(implicit conv: Int => String): String = x
  def defWithImplicitFunctionType2(x: Int)(implicit conv: (Int, Long) => String): String = x
  def defWithImplicitFunctionType3(x: Int)(using conv: (Int, Long) => String): String = x
}
// format: on
