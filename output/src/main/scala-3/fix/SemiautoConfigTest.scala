
package fix

import doobie.*
import io.circe.*
import io.circe.generic.semiauto.*

// format: off
class SemiautoConfigTest {

  case class WithBody(name: String) derives Read
  object WithBody {
    implicit val encoder: Encoder.AsObject[WithBody] = deriveEncoder
    implicit val decoder: Decoder[WithBody] = deriveDecoder
    
  }

}
// format: on
