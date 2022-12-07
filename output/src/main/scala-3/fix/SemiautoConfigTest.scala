package fix

import doobie.*
import io.circe.*
import io.circe.generic.semiauto.*

// format: off
class SemiautoConfigTest {

  case class WithBody(name: String) derives Decoder, Read
  object WithBody {
    implicit val encoder: Encoder.AsObject[WithBody] = deriveEncoder
    
    
  }

}
// format: on
