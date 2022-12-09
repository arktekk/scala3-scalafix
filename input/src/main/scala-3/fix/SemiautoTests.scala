/*
rule = SemiAuto
 */
package fix

import io.circe.*
import io.circe.generic.semiauto.*
import doobie.*

// format: off
object SemiAutoTests {

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

  case class WithBody(name: String) {
    val i: Int = 1
  }
  object WithBody {
    implicit val encoder: Encoder.AsObject[WithBody] = deriveEncoder
    implicit val reads: Read[WithBody] = Read.derived
  }

  case class ScalaGiven(i: Int)
  object ScalaGiven {
    given foo: Read[ScalaGiven] = Read.derived
    given Encoder.AsObject[ScalaGiven] = deriveEncoder
  }

}
// format: on
