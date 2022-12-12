package fix

// format: off
import scala.language.implicitConversions

object GivenAndUsingTest {
  trait Foo[A]

  given fooInt: Foo[Int] = new Foo[Int] {}
  given fooString: Foo[String] = new Foo[String] {}
  given foo(using fooInt: Foo[Int]): Foo[Long] = new Foo[Long] {}

  class Bar[A](i:Int)(using foo: Foo[A], s: String) {
    def f(using iFoo: Foo[Int], sFoo: Foo[String]): Unit = ???
  }
  trait Show[A] {
    def show[A](a: A): String
  }
  object Show {
    def apply[A](using ev: Show[A]): Show[A] = ev
  }
  case class Magnet(s: String)
  implicit def showWithArg[A: Show](a:A): Magnet = Magnet(Show[A].show(a))
  implicit def showWithArgs[A: Show](a: A, s: String): Magnet = Magnet(Show[A].show(a))
}
// format: on
