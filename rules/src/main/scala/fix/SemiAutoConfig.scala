package fix

import metaconfig.ConfDecoder
import metaconfig.generic.Surface

case class SemiAutoConfig(
    bundles: List[String] = List("all"),
) {
  def rewrites: Set[SemiAutoConfig.Rewrite] = {
    if (bundles == List("all")) SemiAutoConfig.rewriteBundles.values.flatten.toSet
    else bundles.flatMap(name => SemiAutoConfig.rewriteBundles.get(name)).flatten.toSet
  }
}

object SemiAutoConfig {
  def default: SemiAutoConfig = SemiAutoConfig()
  implicit val surface: Surface[SemiAutoConfig] =
    metaconfig.generic.deriveSurface[SemiAutoConfig]
  implicit val decoder: ConfDecoder[SemiAutoConfig] =
    metaconfig.generic.deriveDecoder(default)

  private val rewriteBundles = Map(
    "circe" -> List(
      SemiAutoConfig.Rewrite("io.circe.Encoder.AsObject", "Encoder.AsObject"),
      SemiAutoConfig.Rewrite("io.circe.Encoder", "Encoder.AsObject"),
      SemiAutoConfig.Rewrite("io.circe.Decoder", "Decoder"),
      SemiAutoConfig.Rewrite("io.circe.Codec.AsObject", "Codec.AsObject"),
      SemiAutoConfig.Rewrite("io.circe.Codec", "Codec.AsObject"),
    ),
    "doobie" -> List(
      SemiAutoConfig.Rewrite("doobie.util.Read", "Read"),
      SemiAutoConfig.Rewrite("doobie.Types.Read", "Read"),
      SemiAutoConfig.Rewrite("doobie.util.Write", "Write"),
      SemiAutoConfig.Rewrite("doobie.Types.Write", "Write")
    )
  )

  case class Rewrite(
      typeClass: String,
      derived: String
  )

}

