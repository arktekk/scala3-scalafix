package fix

import io.circe.*
import io.circe.generic.semiauto.*

// format: off
object Circescala3 {

  case class Person(name: String, age: Int) derives Encoder.AsObject
  object Person {
    
  }

  case class Person2(name: String, age: Int) derives Encoder.AsObject
  object Person2 {
    
  }

  case class Company(name: String)

  case class Company2(name: String) derives Decoder, Encoder.AsObject
  object Company2 {
    
  }
}
// format: on
