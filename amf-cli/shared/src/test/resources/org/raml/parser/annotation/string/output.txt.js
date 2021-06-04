Model: file://amf-client/shared/src/test/resources/org/raml/parser/annotation/string/input.raml
Profile: RAML 1.0
Conforms? false
Number of results: 3

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be string
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/org/raml/parser/annotation/string/input.raml#/web-api/end-points/%2Ftext/image/scalar_1
  Property: file://amf-client/shared/src/test/resources/org/raml/parser/annotation/string/input.raml#/web-api/end-points/%2Ftext/image/scalar_1
  Position: Some(LexicalInformation([(18,11)-(18,12)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/annotation/string/input.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT be longer than 2 characters
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/org/raml/parser/annotation/string/input.raml#/web-api/end-points/%2Ftext/foo/scalar_1
  Property: file://amf-client/shared/src/test/resources/org/raml/parser/annotation/string/input.raml#/web-api/end-points/%2Ftext/foo/scalar_1
  Position: Some(LexicalInformation([(19,9)-(19,12)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/annotation/string/input.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT be shorter than 10 characters
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/org/raml/parser/annotation/string/input.raml#/web-api/end-points/%2Ftext/tato/scalar_1
  Property: file://amf-client/shared/src/test/resources/org/raml/parser/annotation/string/input.raml#/web-api/end-points/%2Ftext/tato/scalar_1
  Position: Some(LexicalInformation([(20,10)-(20,15)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/annotation/string/input.raml
