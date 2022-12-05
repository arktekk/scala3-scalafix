package fix

import io.circe.*
import io.circe.generic.semiauto.*

// format: off
object Circescala3 {

  case class DeriveEncoderWithTypes(name: String, age: Int) derives Encoder.AsObject
  object DeriveEncoderWithTypes {
    
  }

  case class DeriveEncoder(name: String, age: Int) derives Encoder.AsObject
  object DeriveEncoder {
    
  }

  case class DoNotChange(name: String)

  case class CaseClassWithExistingDerived(name: String) derives Decoder, Encoder.AsObject
  object CaseClassWithExistingDerived {
    
  }

  case class CaseClassWithEncodedNotDerived(name: String)
  object CaseClassWithEncodedNotDerived {
    implicit val enc: Encoder.AsObject[CaseClassWithEncodedNotDerived] = company3 => JsonObject()
  }
}
// format: on
