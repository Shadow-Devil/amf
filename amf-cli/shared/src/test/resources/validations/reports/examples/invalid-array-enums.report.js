Model: file://amf-client/shared/src/test/resources/validations/enums/invalid-array-enums.raml
Profile: RAML 1.0
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be equal to one of the allowed values
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/enums/invalid-array-enums.raml#/declarations/types/array/A/example/invalid1
  Property: file://amf-client/shared/src/test/resources/validations/enums/invalid-array-enums.raml#/declarations/types/array/A/example/invalid1
  Position: Some(LexicalInformation([(10,16)-(10,23)]))
  Location: file://amf-client/shared/src/test/resources/validations/enums/invalid-array-enums.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be equal to one of the allowed values
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/enums/invalid-array-enums.raml#/declarations/types/array/A/example/invalid2
  Property: file://amf-client/shared/src/test/resources/validations/enums/invalid-array-enums.raml#/declarations/types/array/A/example/invalid2
  Position: Some(LexicalInformation([(11,16)-(11,20)]))
  Location: file://amf-client/shared/src/test/resources/validations/enums/invalid-array-enums.raml
