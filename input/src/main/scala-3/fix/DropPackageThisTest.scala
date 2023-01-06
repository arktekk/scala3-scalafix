/*
rule = DropPrivateThis
 */
package fix

// format: off
class Foo() {
  private[this] def foo(a: Int) = ???
  private[this] val bar = 23
  private val baz = "Untouched"
  protected[this] val baz2 = 42
  protected[this] def baz3(b: Boolean = true) = "touched"
}