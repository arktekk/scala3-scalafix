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
class MyClass2 { def myClass: MyClass = ??? }
object ToImport {
  given myClass: MyClass = ???
  given myClass2: MyClass2 = ???
}
object ModifyImport {
  import ToImport.{ given, * }
  def useMyClass1(using myClass: MyClass): String = ???
  def useMyClass2(i: Int)(using myClass: MyClass): String = ???
  val mc1 = useMyClass1
  val mc2 = useMyClass1(using new MyClass)
  val mc3 = useMyClass2(1)(using new MyClass)
}
object DoNotModifyImport {
  import ToImport.{ given, *}
  def useMyClass(using myClass: MyClass2): String = ???
  val mc = useMyClass
}
class ClassWithTwoImplicitArgs(i: Int, implicit val keepImplicit: Boolean, s: String)(using ec: scala.concurrent.ExecutionContext)
class ClassWithImplicitAndGivenArgs(i: Int, implicit val keepImplicit: Boolean, s: String)(using ec: scala.concurrent.ExecutionContext)
object ObjectWithApply {
  object inner1 {
    def apply(s: String)(using myClass2: MyClass2) = ???
  }
  object inner2 {
    def apply(using myClass2: MyClass2): String => String = ???
  }
  inner1("")(using new MyClass2)
  inner2(using new MyClass2)("")
  def call1(myClass2: MyClass2) = inner1("")(using myClass2)
  def call2(myClass2: MyClass2) = inner2(using myClass2)("")
}
// format: on
