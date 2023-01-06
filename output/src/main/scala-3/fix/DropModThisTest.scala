package fix

// format: off
class DropModThisTest() {
  private def foo(a: Int) = ???
  private val bar = 23
  private val baz = "Untouched"
  protected val baz2 = 42
  protected def baz3(b: Boolean = true) = "touched"
}