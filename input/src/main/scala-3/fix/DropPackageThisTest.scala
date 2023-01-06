/*
rule = DropPrivateThis
 */
package fix

// format: off
class Foo() {
  private[this] def foo(a: Int) = ???
  private[this] val bar = 23
  private val baz = "Untouched"
}