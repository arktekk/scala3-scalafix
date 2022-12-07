/*
rule = SemiAuto
SemiAuto.bundles = [doobie]
SemiAuto.rewrites = [
  { typeClass = "io.circe.Decoder", derived =  "Decoder" }
]
 */
package fix

import doobie.*
import io.circe.*
import io.circe.generic.semiauto.*

// format: off
class SemiautoConfigTest {

  case class WithBody(name: String)
  object WithBody {
    implicit val encoder: Encoder.AsObject[WithBody] = deriveEncoder// assert: SemiAuto
    implicit val decoder: Decoder[WithBody] = deriveDecoder
    implicit val reads: Read[WithBody] = Read.derived
  }

}
// format: on
