Model: file://amf-client/shared/src/test/resources/validations/discriminator/discriminator-array-items.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: [0] should have required property 'anotherProp'
[1] should NOT have additional properties

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/discriminator/discriminator-array-items.raml#/web-api/end-points/%2Finvalid/put/request/array/default/example/default-example
  Property: file://amf-client/shared/src/test/resources/validations/discriminator/discriminator-array-items.raml#/web-api/end-points/%2Finvalid/put/request/array/default/example/default-example
  Position: Some(LexicalInformation([(39,0)-(42,58)]))
  Location: file://amf-client/shared/src/test/resources/validations/discriminator/discriminator-array-items.raml
