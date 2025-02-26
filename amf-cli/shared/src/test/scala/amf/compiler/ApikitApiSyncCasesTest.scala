package amf.compiler

import amf.apicontract.client.scala.{RAMLConfiguration, WebAPIConfiguration}
import amf.core.client.common.remote.Content
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.DefaultErrorHandler
import amf.core.client.scala.resource.ResourceLoader
import amf.core.internal.parser.{AMFCompiler, CompilerConfiguration}
import amf.core.internal.remote.{Cache, Context, FileNotFound}
import amf.core.internal.unsafe.PlatformSecrets
import org.mulesoft.common.test.AsyncBeforeAndAfterEach
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, Future}

class ApikitApiSyncCasesTest extends AsyncBeforeAndAfterEach with PlatformSecrets with Matchers {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Resource loader shouldn't have absolute path if ref and base aren't absolute") {
    val mappings = Map(
      "resource::really-cool-urn:1.0.0:raml:zip:main.raml" -> CustomContentResult(
        "file://amf-cli/shared/src/test/resources/compiler/apikit-apisync/ref-base-not-absolute/main.raml"
      ),
      "something.raml" -> CustomContentResult(
        "file://amf-cli/shared/src/test/resources/compiler/apikit-apisync/ref-base-not-absolute/something.raml"
      ),
      "examples/something/get-something-response.raml" -> CustomContentResult(
        "file://amf-cli/shared/src/test/resources/compiler/apikit-apisync/ref-base-not-absolute/examples/something/get-something-response.raml"
      ),
      "examples/something/put-something-request.raml" -> CustomContentResult(
        "file://amf-cli/shared/src/test/resources/compiler/apikit-apisync/ref-base-not-absolute/examples/something/put-something-request.raml"
      ),
      "examples/common/async-response.raml" -> CustomContentResult(
        "file://amf-cli/shared/src/test/resources/compiler/apikit-apisync/ref-base-not-absolute/examples/common/async-response.raml"
      ),
      "libraries/resourceTypes.raml" -> CustomContentResult(
        "file://amf-cli/shared/src/test/resources/compiler/apikit-apisync/ref-base-not-absolute/libraries/resourceTypes.raml"
      )
    )
    val url = "resource::really-cool-urn:1.0.0:raml:zip:main.raml"
    val eh  = DefaultErrorHandler()
    AMFCompiler(
      url,
      base = Context(platform),
      cache = Cache(),
      CompilerConfiguration(
        WebAPIConfiguration
          .WebAPI()
          .withResourceLoaders(List(new URNResourceLoader(mappings)))
          .withErrorHandlerProvider(() => eh)
      )
    ).build()
      .map { _ =>
        eh.getResults should have size 0
      }
  }

  // APIMF-3118
  test("Resource loader should find nested references") {
    val mappings = Map(
      "resource::37fab092-be99-4538-b5ce-b004c5439f6d:refexample:1.0.1:oas:zip:example.json" -> CustomContentResult(
        "file://amf-cli/shared/src/test/resources/compiler/apikit-apisync/uri-file-prefix/example.json"
      ),
      "exchange.json" -> CustomContentResult(
        "file://amf-cli/shared/src/test/resources/compiler/apikit-apisync/uri-file-prefix/exchange.json"
      ),
      "components/schemas/okResponse.json" -> CustomContentResult(
        "file://amf-cli/shared/src/test/resources/compiler/apikit-apisync/uri-file-prefix/components/schemas/okResponse.json"
      ),
      "components/schemas/item.json" -> CustomContentResult(
        "file://amf-cli/shared/src/test/resources/compiler/apikit-apisync/uri-file-prefix/components/schemas/item.json"
      ),
      "components/schemas/nested.json" -> CustomContentResult(
        "file://amf-cli/shared/src/test/resources/compiler/apikit-apisync/uri-file-prefix/components/schemas/nested.json"
      )
    )
    val url = "resource::37fab092-be99-4538-b5ce-b004c5439f6d:refexample:1.0.1:oas:zip:example.json"
    val client = WebAPIConfiguration
      .WebAPI()
      .withResourceLoaders(List(new URNResourceLoader(mappings)))
      .baseUnitClient()
    client.parse(url).map { parseResult =>
      parseResult.results should have size 0
    }
  }

  test("Parsing context generated in extends resolution stage for raml traits") {
    val mappings = Map(
      "resource::really-cool-urn:1.0.0:raml:zip:townfile.raml" -> CustomContentResult(
        "file://amf-cli/shared/src/test/resources/compiler/apikit-apisync/extends-stage-traits/townfile.raml"
      ),
      "some-modules/for-health-check.raml" -> CustomContentResult(
        "file://amf-cli/shared/src/test/resources/compiler/apikit-apisync/extends-stage-traits/some-modules/for-health-check.raml"
      ),
      "some-modules/trait.raml" -> CustomContentResult(
        "file://amf-cli/shared/src/test/resources/compiler/apikit-apisync/extends-stage-traits/some-modules/trait.raml"
      )
    )
    val url = "resource::really-cool-urn:1.0.0:raml:zip:townfile.raml"
    val eh  = DefaultErrorHandler()
    val client = RAMLConfiguration
      .RAML10()
      .withResourceLoaders(List(new URNResourceLoader(mappings)))
      .withErrorHandlerProvider(() => eh)
      .baseUnitClient()
    for {
      parseResult <- client.parse(url)
      _           <- Future.successful(client.transform(parseResult.baseUnit, PipelineId.Editing))
    } yield {
      eh.getResults should have size 0
    }
  }

  // APIMF-3384
  test("should accept content URLs starting with 'jar:'") {
    val mappings = Map(
      "resource::com.mycompany:consumer-api:1.0.0:oas:zip:consumer.yaml" -> CustomContentResult(
        "file://amf-cli/shared/src/test/resources/compiler/apikit-apisync/jar-protocol/consumer.yaml"
      ),
      "utility.yaml" -> CustomContentResult(
        "file://amf-cli/shared/src/test/resources/compiler/apikit-apisync/jar-protocol/utility.yaml"
      )
    )
    val url = "resource::com.mycompany:consumer-api:1.0.0:oas:zip:consumer.yaml"
    val client = WebAPIConfiguration
      .WebAPI()
      .withResourceLoaders(List(new URNResourceLoader(mappings)))
      .baseUnitClient()
    client.parse(url).map { parseResult =>
      parseResult.results should have size 0
    }
  }

  // APIMF-3600
  test("exchange modules with multiple zip sources") {
    val mappings = Map(
      "resource::8fe5354c-e64c-4eaa-addc-b50906a0b48c:se-23375:1.0.1:oas:zip:se-23375.json" -> CustomContentResult(
        "file://amf-cli/shared/src/test/resources/compiler/apikit-apisync/exchange-modules/se-23375.json"
      ),
      "exchange_modules/8fe5354c-e64c-4eaa-addc-b50906a0b48c/datamodel-tmforum/1.0.0/4.1.0/Customer/Bucket.schema.json" -> CustomContentResult(
        "file://amf-cli/shared/src/test/resources/compiler/apikit-apisync/exchange-modules/Bucket.schema.json"
      )
    )
    val url = "resource::8fe5354c-e64c-4eaa-addc-b50906a0b48c:se-23375:1.0.1:oas:zip:se-23375.json"
    val client = WebAPIConfiguration
      .WebAPI()
      .withResourceLoaders(List(new URNResourceLoader(mappings)))
      .baseUnitClient()
    client.parse(url).map { parseResult =>
      parseResult.results should have size 0
    }
  }

  // APIMF-3533
  test("references to external yaml using './'") {
    val mappings = Map(
      "resource::8fe5354c-e64c-4eaa-addc-b50906a0b48c:pure-member:1.0.0:oas:zip:pure-member-portal.yaml" -> CustomContentResult(
        "file://amf-cli/shared/src/test/resources/compiler/apikit-apisync/dot-slash-ref/pure-member-portal.yaml"
      ),
      "responses.yaml" -> CustomContentResult(
        "file://amf-cli/shared/src/test/resources/compiler/apikit-apisync/dot-slash-ref/responses.yaml"
      )
    )
    val url = "resource::8fe5354c-e64c-4eaa-addc-b50906a0b48c:pure-member:1.0.0:oas:zip:pure-member-portal.yaml"
    val client = WebAPIConfiguration
      .WebAPI()
      .withResourceLoaders(List(new URNResourceLoader(mappings)))
      .baseUnitClient()
    client.parse(url).map { parseResult =>
      parseResult.results should have size 0
    }
  }

  case class CustomContentResult(actualPath: String)

  class URNResourceLoader(mappings: Map[String, CustomContentResult]) extends ResourceLoader {

    override def fetch(resource: String): Future[Content] = {
      mappings
        .get(resource)
        .map(url => {
          platform
            .fetchContent(url.actualPath, AMFGraphConfiguration.predefined())
            .map(content => Content(content.stream, resource)) // content result url provided by resource loader
        })
        .getOrElse(throw FileNotFound(new RuntimeException(s"Couldn't find resource $resource")))
    }

    override def accepts(resource: String): Boolean = true
  }
}
