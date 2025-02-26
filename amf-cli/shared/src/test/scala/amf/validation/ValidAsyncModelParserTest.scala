package amf.validation

import amf.apicontract.client.scala.AsyncAPIConfiguration
import amf.core.client.common.validation.Async20Profile
import amf.core.internal.remote.{Async20YamlHint, Hint}

class ValidAsyncModelParserTest extends ValidModelTest {

  test("Full message object") {
    checkValid("message-obj.yaml")
  }

  test("Draft 7 schemas") {
    checkValid("draft-7-schemas.yaml")
  }

  test("Channel parameters") {
    checkValid("channel-parameters.yaml")
  }

  test("Security schemes") {
    checkValid("security-schemes.yaml")
  }

  test("Empty and dynamic binding") {
    checkValid("empty-binding-and-annotations.yaml")
  }

  test("Amqp 0.9.1 channel binding") {
    checkValid("amqp-channel-binding.yaml")
  }

  test("Amqp 0.9.1 message binding") {
    checkValid("amqp-message-binding.yaml")
  }

  test("Amqp 0.9.1 operation binding") {
    checkValid("amqp-operation-binding.yaml")
  }

  test("Http message binding") {
    checkValid("http-message-binding.yaml")
  }

  test("Http operation binding") {
    checkValid("http-operation-binding.yaml")
  }

  test("Kafka message binding") {
    checkValid("kafka-message-binding.yaml")
  }

  test("Kafka operation binding") {
    checkValid("kafka-operation-binding.yaml")
  }

  test("Mqtt message binding") {
    checkValid("mqtt-message-binding.yaml")
  }

  test("Mqtt operation binding") {
    checkValid("mqtt-operation-binding.yaml")
  }

  test("Mqtt server binding") {
    checkValid("mqtt-server-binding.yaml")
  }

  test("Websockets channel binding") {
    checkValid("ws-channel-binding.yaml")
  }

  test("Rpc server example") {
    checkValid("rpc-server.yaml")
  }

  test("Amqp channel binding") {
    checkValid("amqp-channel-binding.yaml")
  }

  test("References to message defined in components") {
    checkValid("reference-declared-messages.yaml")
  }

  test("Reference to external raml data type fragment with includes at root of payload") {
    checkValid("raml-data-type-references/include-data-type-at-root-of-payload.yaml")
  }

  test("Reference to external raml data type fragment with includes inlined in payload") {
    checkValid("raml-data-type-references/include-data-type-inlined-in-payload.yaml")
  }

  test("Referencing raml content with $ref - data type fragment") {
    checkValid("raml-data-type-references/ref-data-type-fragment.yaml")
  }

  test("Referencing raml content with $ref - type defined in library") {
    checkValid("raml-data-type-references/ref-type-in-library.yaml")
  }

  test("Referencing raml content with $ref - type defined in external yaml") {
    checkValid("raml-data-type-references/ref-external-yaml.yaml")
  }

  test("Referencing raml content with $ref - type defined in external json") {
    checkValid("raml-data-type-references/ref-external-json.yaml")
  }

  test("Referencing raml content with $ref without specifying format - async config") {
    checkValid(
      "raml-data-type-references/ref-without-schema-format.yaml",
      Async20Profile,
      Some(AsyncAPIConfiguration.Async20())
    )
  }

  test("Referencing raml content with $ref without specifying format - composite config") {
    checkValid("raml-data-type-references/ref-without-schema-format.yaml")
  }

  override val basePath: String = "file://amf-cli/shared/src/test/resources/validations/async20/"

}
