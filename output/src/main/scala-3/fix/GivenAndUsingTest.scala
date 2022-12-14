package fix

// format: off
import scala.language.implicitConversions

trait Show[A] {
  def show[A](a: A): String
}
object Show {
  def apply[A](using ev: Show[A]): Show[A] = ev
}
object GivenAndUsingTest {
  trait Foo[A]

  given fooInt: Foo[Int] = new Foo[Int] {}
  given fooString: Foo[String] = new Foo[String] {}
  given fooParam[A: Foo]: Foo[List[A]] = new Foo[List[A]] {}
  given foo(using fooInt: Foo[Int]): Foo[Long] = new Foo[Long] {}
  given fooUsing(using fooInt: Foo[Int]): Foo[Long] = new Foo[Long] {}

  class Bar[A](i:Int)(using foo: Foo[A], s: String) {
    def f(using iFoo: Foo[Int], sFoo: Foo[String]): Unit = ???
  }
  case class Magnet(s: String)
  implicit def showWithArg[A: Show](a:A): Magnet = Magnet(Show[A].show(a))
  implicit def showWithArgs[A: Show](a: A, s: String): Magnet = Magnet(Show[A].show(a))
  def untypedImplicit[A](foo: Foo[A]): Unit = {
    implicit val fooOfA = foo
  }
}
class MyClass
class MyClass2
object ToImport {
  given myClass: MyClass = ???
  given myClass2: MyClass2 = ???
}
object ModifyImport {
  import ToImport.{ given, * }
  def useMyClass(using myClass: MyClass): String = ???
  val mc = useMyClass
}
object DoNotModifyImport {
  import ToImport.{ given, *}
  def useMyClass(using myClass: MyClass2): String = ???
  val mc = useMyClass
}
// format: on
