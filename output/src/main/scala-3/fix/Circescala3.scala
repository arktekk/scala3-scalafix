package fix

import io.circe.*
import io.circe.generic.semiauto.*
import doobie.*

// format: off
object Circescala3 {

  case class DeriveEncoderWithTypes(name: String, age: Int) derives Encoder.AsObject
  

  case class DeriveEncoder(name: String, age: Int) derives Encoder.AsObject
  

  case class DeriveEncoderAndKeepCompanion(name: String, age: Int) derives Encoder.AsObject
  object DeriveEncoderAndKeepCompanion {
    val i: Int = 1
    
  }

  case class DoNotChange(name: String)

  case class CaseClassWithExistingDerived(name: String) derives Decoder, Encoder.AsObject
  

  case class CaseClassWithEncodedNotDerived(name: String)
  object CaseClassWithEncodedNotDerived {
    implicit val enc: Encoder.AsObject[CaseClassWithEncodedNotDerived] = company3 => JsonObject()
  }

  trait Foo
  case class WithExtendAndBody(name: String) extends Foo derives Encoder.AsObject {
    val i: Int = 1
  }
  

  case class WithBody(name: String) derives Encoder.AsObject, Read {
    val i: Int = 1
  }
  
}
// format: on
