Model: file://amf-client/shared/src/test/resources/validations/resource_types/optionalMethodAsParameterValue.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be <= 10
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/resource_types/optionalMethodAsParameterValue.raml#/web-api/end-points/%2Fe1/post/request/application%2Fjson/any/schema/example/default-example
  Property: file://amf-client/shared/src/test/resources/validations/resource_types/optionalMethodAsParameterValue.raml#/web-api/end-points/%2Fe1/post/request/application%2Fjson/any/schema/example/default-example
  Position: Some(LexicalInformation([(22,17)-(22,19)]))
  Location: file://amf-client/shared/src/test/resources/validations/resource_types/optionalMethodAsParameterValue.raml
