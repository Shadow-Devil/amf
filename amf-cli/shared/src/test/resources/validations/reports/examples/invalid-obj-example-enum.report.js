Model: file://amf-client/shared/src/test/resources/validations/enums/invalid-obj-example-enum.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be equal to one of the allowed values
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/enums/invalid-obj-example-enum.raml#/declarations/types/A/example/default-example
  Property: file://amf-client/shared/src/test/resources/validations/enums/invalid-obj-example-enum.raml#/declarations/types/A/example/default-example
  Position: Some(LexicalInformation([(14,0)-(15,16)]))
  Location: file://amf-client/shared/src/test/resources/validations/enums/invalid-obj-example-enum.raml
