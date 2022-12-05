/*
rule = Circescala3
 */
package fix

import io.circe.*
import io.circe.generic.semiauto.*

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

  case class Company2(name: String) derives Decoder
  object Company2 {
    implicit val encoder: Encoder.AsObject[Company2] = deriveEncoder
  }
}
// format: on