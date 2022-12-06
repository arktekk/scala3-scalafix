/*
rule = Circescala3
 */
package fix

import io.circe.*
import io.circe.generic.semiauto.*

// format: off
object Circescala3 {

  case class DeriveEncoderWithTypes(name: String, age: Int)
  object DeriveEncoderWithTypes {
    implicit val encoder: Encoder.AsObject[DeriveEncoderWithTypes] = deriveEncoder[DeriveEncoderWithTypes]
  }

  case class DeriveEncoder(name: String, age: Int)
  object DeriveEncoder {
    implicit val encoder: Encoder.AsObject[DeriveEncoder] = deriveEncoder
  }

  case class DeriveEncoderAndKeepCompanion(name: String, age: Int)
  object DeriveEncoderAndKeepCompanion {
    val i: Int = 1
    implicit val encoder: Encoder.AsObject[DeriveEncoderAndKeepCompanion] = deriveEncoder
  }

  case class DoNotChange(name: String)

  case class CaseClassWithExistingDerived(name: String) derives Decoder
  object CaseClassWithExistingDerived {
    implicit val encoder: Encoder.AsObject[CaseClassWithExistingDerived] = deriveEncoder
  }

  case class CaseClassWithEncodedNotDerived(name: String)
  object CaseClassWithEncodedNotDerived {
    implicit val enc: Encoder.AsObject[CaseClassWithEncodedNotDerived] = company3 => JsonObject()
  }

  trait Foo
  case class WithExtendAndBody(name: String) extends Foo {
    val i: Int = 1
  }
  object WithExtendAndBody {
    implicit val encoder: Encoder.AsObject[WithExtendAndBody] = deriveEncoder
  }
}
// format: on