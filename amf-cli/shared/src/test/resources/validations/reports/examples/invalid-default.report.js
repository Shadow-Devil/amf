ModelId: file://amf-cli/shared/src/test/resources/validations/examples/invalid-default.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should match pattern "^[a-z]*$"
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/invalid-default.raml#/declares/scalar/name/scalar_1
  Property: file://amf-cli/shared/src/test/resources/validations/examples/invalid-default.raml#/declares/scalar/name/scalar_1
  Range: [(7,13)-(7,20)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/invalid-default.raml
