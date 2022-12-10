package fix

// format: off
object GivenAndUsingTest {
  trait Foo[A]

  given fooInt: Foo[Int] = new Foo[Int] {}
  given fooString: Foo[String] = new Foo[String] {}
  given foo(using fooInt: Foo[Int]): Foo[Long] = new Foo[Long] {}

  class Bar[A](i:Int)(using foo: Foo[A], s: String) {
    def f(using iFoo: Foo[Int], sFoo: Foo[String]): Unit = ???
  }
}
// format: on
