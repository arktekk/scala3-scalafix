/*
rule = GivenAndUsing
 */

package fix

// format: off
object GivenAndUsingTest {
  trait Foo[A]

  implicit val fooInt: Foo[Int] = new Foo[Int] {}
  implicit def fooString: Foo[String] = new Foo[String] {}
  implicit def foo(implicit fooInt: Foo[Int]): Foo[Long] = new Foo[Long] {}

  class Bar[A](i:Int)(implicit foo: Foo[A], s: String) {
    def f(implicit iFoo: Foo[Int], sFoo: Foo[String]): Unit = ???
  }
}
// format: on
