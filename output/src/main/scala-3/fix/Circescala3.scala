package fix

import io.circe._
import io.circe.generic.semiauto._

object Circescala3 {

  case class Person(name: String, age: Int) derives Encoder.AsObject

  object Person {

  }

  case class Company(name: String)
}