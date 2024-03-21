package fix

import scala.language.implicitConversions

// format: off
object ImplicitConversionsTest {
  implicit val implicitDefWithFunctionArg1: Conversion[Int, String] = (x: Int) => x.toString
  implicit val implicitDefWithFunctionArg2: Conversion[(Int, Long), String] = (x: (Int, Long)) => x.toString

  implicit val implicitValWithFunctionType1: Conversion[Long, String] = _.toString
  implicit val implicitValWithFunctionType2: Conversion[(Int, Long), String] = (i, l ) => i.toString

  def defWithImplicitFunctionType1(x: Int)(implicit conv: Conversion[Int, String]): String = x
  def defWithImplicitFunctionType2(x: Int)(implicit conv: Conversion[(Int, Long), String]): String = x
  def defWithImplicitFunctionType3(x: Int)(using conv: Conversion[(Int, Long), String]): String = x
}
// format: on
