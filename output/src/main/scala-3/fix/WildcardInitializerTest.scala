package fix

// format: off
class Foo{
  var n: Int = scala.compiletime.uninitialized
  var s: String = "untouched"
}
