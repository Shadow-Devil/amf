package amf.validation

import amf.core.client.common.validation.{Raml08Profile, Raml10Profile}
import amf.core.internal.remote.{Hint, Raml08YamlHint, Raml10YamlHint}

class RamlModelMultiPlatformReportTest extends MultiPlatformReportGenTest {

  test("Test non existing include in type def") {
    validate("/missing-includes/in-type-def.raml", Some("missing-includes/in-type-def.report"))
  }

  test("Test non existing include in resource type def") {
    validate(
      "/missing-includes/in-resource-type-def.raml",
      Some("missing-includes/in-resource-type-def.report")
    )
  }

  test("Test non existing include in trait def") {
    validate(
      "/missing-includes/in-trait-def.raml",
      Some("missing-includes/in-trait-def.report")
    )
  }

  test("Facet minimum and maximum with left zeros") {
    validate("/facets/min-max-zeros.raml", Some("min-max-zeros.report"))
  }

  test("Facet minimum and maximum with left zeros other") {
    validate("/facets/min-max-zeros-other.raml", Some("min-max-zeros-other.report"))
  }

  test("Discriminator with closed parent") {
    validate("discriminator/invalid/closed-parent.raml", Some("discriminator-closed-parent.report"))
  }

  test("Discriminator with additional enum values") {
    validate("discriminator/invalid/additional-enum-values.raml", Some("discriminator-additional-enum-values.report"))
  }

  test("Discriminator in array items") {
    validate("discriminator/discriminator-array-items.raml", Some("discriminator-array-items.report"))
  }

  test("Annotations are validated when contained in scalar valued nodes") {
    validate("annotations/annotating-scalar-valued-nodes.raml", Some("annotating-scalar-valued-nodes.report"))
  }

  test("Invalid values in user defined facets") {
    validate("facets/invalid-facet-value-type.raml", Some("invalid-facet-value-type.report"))
  }

  test("Raml overlay with invalid example without overloading type") {
    validate(
      "overlays/overlay-with-inferred-type-invalid-example/overlay.raml",
      Some("overlay-with-inferred-type-invalid-example.report")
    )
  }

  test("Annotation type with invalid example") {
    validate("annotation-types-invalid-example.raml", Some("annotation-types-invalid-example.report"))
  }

  test("nested json schema defined in external fragment") {
    validate(
      "raml/nested-json-schema-external-fragment/api.raml",
      Some("invalid-example-result.report")
    )
  }

  test("maximum/minimum validation with 17 digit numbers") {
    validate("raml/big-number-examples.raml", Some("invalid-big-numbers.report"))
  }

  test("Reffed additional properties in JSON Schema subschema are validated") {
    validate("raml/reffed-additional-properties/api.raml", Some("reffed-additional-properties.report"))
  }

  test("invalid example in union with members of the same type with enums") {
    validate(
      "raml/invalid-example-in-union-with-members-with-enums.raml",
      Some("invalid-example-in-union-with-members-with-enums.report")
    )
  }

  override val basePath    = "file://amf-cli/shared/src/test/resources/validations/"
  override val reportsPath = "amf-cli/shared/src/test/resources/validations/reports/multi-plat-model/"

}
