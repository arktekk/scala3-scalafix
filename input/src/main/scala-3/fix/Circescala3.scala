/*
rule = Circescala3
*/
package fix

import io.circe._
import io.circe.generic.semiauto._

object Circescala3 {

  case class Person(name: String, age: Int)

  object Person {
    implicit val encoder: Encoder.AsObject[Person] = deriveEncoder[Person]
  }

  case class Company(name: String)
}
