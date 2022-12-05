package fix

import io.circe._
import io.circe.generic.semiauto._

// format: off
object Circescala3 {

  case class Person(name: String, age: Int) derives Encoder.AsObject
  object Person {
    
  }

  case class Person2(name: String, age: Int) derives Encoder.AsObject
  object Person2 {
    
  }

  case class Company(name: String)
}
// format: on
