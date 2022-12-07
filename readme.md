# Scala 3 Scalafix rules

## SemiAuto

Rewrite implicit derived values to scala 3 syntax

**Configuration:**

```hocon
// default config
SemiAuto.bundle = [all]

// option to specify a bundle of rewrites
SemiAuto.bundle = [circe, doobie]

//Bring your own rewrites.
SemiAuto.rewrites = [
  { typeClass = "my.Decoder", derived =  "Decoder" }
  { typeClass = "my.Encoder", derived =  "Encoder" }
]
```

**Example:**

```scala
// Before
case class Person(name: String)
object Person{
  implicit val codec: Codec.AsObject[Person] = deriveEncoder
}
// After
case class Person(name: String) derives Codec.AsObject
```

