/*
rule = GivenAndUsing
 */

package fix

// format: off
import scala.language.implicitConversions

object GivenAndUsingTest {
  trait Foo[A]

  implicit val fooInt: Foo[Int] = new Foo[Int] {}
  implicit def fooString: Foo[String] = new Foo[String] {}
  implicit def foo(implicit fooInt: Foo[Int]): Foo[Long] = new Foo[Long] {}
  implicit def fooUsing(using fooInt: Foo[Int]): Foo[Long] = new Foo[Long] {}

  class Bar[A](i:Int)(implicit foo: Foo[A], s: String) {
    def f(implicit iFoo: Foo[Int], sFoo: Foo[String]): Unit = ???
  }
  trait Show[A] {
    def show[A](a: A): String
  }
  object Show {
    def apply[A](implicit ev: Show[A]): Show[A] = ev
  }
  case class Magnet(s: String)
  implicit def showWithArg[A: Show](a:A): Magnet = Magnet(Show[A].show(a))/* assert: GivenAndUsing
                                    ^^^
  Unable to rewrite to `given` syntax because we found a function with a non implicit argument.
*/
  implicit def showWithArgs[A: Show](a: A, s: String): Magnet = Magnet(Show[A].show(a))/* assert: GivenAndUsing
                                     ^^^^^^^^^^^^^^^
  Unable to rewrite to `given` syntax because we found a function with a non implicit argument.
  */
  def untypedImplicit[A](foo: Foo[A]): Unit = {
    implicit val fooOfA = foo// assert: GivenAndUsing
  }
}
// format: on
