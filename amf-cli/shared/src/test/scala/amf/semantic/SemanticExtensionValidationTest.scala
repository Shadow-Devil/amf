package amf.semantic

import amf.apicontract.client.scala.{
  AMFConfiguration,
  APIConfiguration,
  AsyncAPIConfiguration,
  OASConfiguration,
  RAMLConfiguration
}
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.{DefaultErrorHandler, UnhandledErrorHandler}
import amf.core.internal.remote.{AmfJsonHint, Async20YamlHint, Hint, Oas20YamlHint, Oas30YamlHint, Raml10YamlHint}
import amf.validation.{MultiPlatformReportGenTest, UniquePlatformReportGenTest}

import scala.concurrent.{ExecutionContext, Future}

class SemanticExtensionValidationTest extends MultiPlatformReportGenTest {

  override val basePath: String    = "file://amf-cli/shared/src/test/resources/semantic/validation/"
  override val reportsPath: String = "amf-cli/shared/src/test/resources/semantic/validation/reports/"

  test("Validate scalar semantic extensions in RAML 1.0 api") {
    getConfig("dialect.yaml", RAMLConfiguration.RAML10()).flatMap { config =>
      validate("api.raml", Some("api.raml.report"), configOverride = Some(config))
    }
  }

  test("Validate scalar semantic extensions in OAS 2.0 api") {
    getConfig("dialect.yaml", OASConfiguration.OAS20()).flatMap { config =>
      validate(
        "api.oas20.yaml",
        Some("api.oas20.report"),
        configOverride = Some(config)
      )
    }
  }

  test("Validate scalar semantic extensions in OAS 3.0 api") {
    getConfig("dialect.yaml", OASConfiguration.OAS30()).flatMap { config =>
      validate(
        "api.oas30.yaml",
        Some("api.oas30.report"),
        configOverride = Some(config)
      )
    }
  }

  test("Validate scalar semantic extensions in ASYNC 2.0 api") {
    getConfig("dialect.yaml", AsyncAPIConfiguration.Async20()).flatMap { config =>
      validate(
        "api.async.yaml",
        Some("api.async.report"),
        configOverride = Some(config)
      )
    }
  }

  test("Validate unresolved links in SemEx") {
    getConfig("object-extension.yaml", OASConfiguration.OAS30()).flatMap { config =>
      validate(
        "unresolved-link-api.oas30.yaml",
        Some("unresolved-link-api.oas30.report"),
        configOverride = Some(config.withErrorHandlerProvider(() => DefaultErrorHandler()))
      )
    }
  }

  test("Validate missing annotation definition at a RAML 1.0 SemEx") {
    getConfig("dialect.yaml", RAMLConfiguration.RAML10()).flatMap { config =>
      validate(
        "api-without-schema.raml",
        Some("api-without-schema.raml.report"),
        configOverride = Some(config)
      )
    }
  }

  test("Validate invalid (not any) annotation definition at a RAML 1.0 SemEx") {
    getConfig("dialect.yaml", RAMLConfiguration.RAML10()).flatMap { config =>
      validate(
        "api-not-any-schema.raml",
        Some("api-not-any-schema.raml.report"),
        configOverride = Some(config)
      )
    }
  }

  test("Validate valid multiple SemEx in the same branch at a RAML 1.0") {
    getConfig("../dialect-endpoint-operation.yaml", RAMLConfiguration.RAML10()).flatMap { config =>
      validate(
        "../api-endpoint-operation.raml",
        None,
        configOverride = Some(config)
      )
    }
  }

  private def getConfig(
      dialect: String,
      baseConfig: AMFConfiguration = APIConfiguration.API()
  ): Future[AMFConfiguration] = {
    baseConfig
      .withRenderOptions(RenderOptions().withPrettyPrint.withCompactUris)
      .withDialect(s"$basePath" + dialect)
  }
}
