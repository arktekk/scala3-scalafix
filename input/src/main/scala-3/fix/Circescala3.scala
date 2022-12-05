/*
rule = Circescala3
 */
package fix

import io.circe._
import io.circe.generic.semiauto._

// format: off
object Circescala3 {

  case class Person(name: String, age: Int)
  object Person {
    implicit val encoder: Encoder.AsObject[Person] = deriveEncoder[Person]
  }

  case class Person2(name: String, age: Int)
  object Person2 {
    implicit val encoder: Encoder.AsObject[Person2] = deriveEncoder
  }

  case class Company(name: String)
}
// format: on