ModelId: file://amf-cli/shared/src/test/resources/validations/examples/single-complex-inheritance.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT have more than 3 properties
should have required property 'numberOfUSBPorts'
should match some schema in anyOf

  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/single-complex-inheritance.raml#/web-api/endpoint/%2Ftest/supportedOperation/get/returns/resp/200/payload/application%2Fjson/shape/schema/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/examples/single-complex-inheritance.raml#/web-api/endpoint/%2Ftest/supportedOperation/get/returns/resp/200/payload/application%2Fjson/shape/schema/example/default-example
  Range: [(33,0)-(36,23)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/single-complex-inheritance.raml
