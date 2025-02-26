package amf.shapes.client.scala.config

import amf.aml.client.scala.AMLConfigurationState
import amf.aml.client.scala.model.document.{Dialect, DialectInstance}
import amf.aml.internal.registries.AMLRegistry
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.config._
import amf.core.client.scala.errorhandling.{DefaultErrorHandlerProvider, ErrorHandlerProvider}
import amf.core.client.scala.execution.ExecutionEnvironment
import amf.core.client.scala.model.domain.AnnotationGraphLoader
import amf.core.client.scala.parse.AMFParsePlugin
import amf.core.client.scala.resource.ResourceLoader
import amf.core.client.scala.transform.TransformationPipeline
import amf.core.internal.metamodel.ModelDefaultBuilder
import amf.core.internal.plugins.AMFPlugin
import amf.core.internal.plugins.parse.DomainParsingFallback
import amf.core.internal.registries.AMFRegistry
import amf.core.internal.resource.AMFResolvers
import amf.core.internal.validation.EffectiveValidations
import amf.core.internal.validation.core.ValidationProfile
import amf.shapes.client.scala.{ShapesConfiguration, ShapesElementClient}
import amf.shapes.internal.spec.jsonschema.semanticjsonschema.JsonSchemaDialectParsePlugin

import scala.concurrent.{ExecutionContext, Future}

class SemanticJsonSchemaConfiguration private[amf] (
    override private[amf] val resolvers: AMFResolvers,
    override private[amf] val errorHandlerProvider: ErrorHandlerProvider,
    override private[amf] val registry: AMLRegistry,
    override private[amf] val listeners: Set[AMFEventListener],
    override private[amf] val options: AMFOptions
) extends ShapesConfiguration(resolvers, errorHandlerProvider, registry, listeners, options) {

  private implicit val ec: ExecutionContext = this.getExecutionContext

  override protected[amf] def copy(
      resolvers: AMFResolvers = resolvers,
      errorHandlerProvider: ErrorHandlerProvider = errorHandlerProvider,
      registry: AMFRegistry = registry,
      listeners: Set[AMFEventListener] = listeners,
      options: AMFOptions = options
  ): SemanticJsonSchemaConfiguration =
    new SemanticJsonSchemaConfiguration(
      resolvers,
      errorHandlerProvider,
      registry.asInstanceOf[AMLRegistry],
      listeners,
      options
    )

  /** Contains common AMF graph operations associated to documents */
  override def baseUnitClient(): SemanticBaseUnitClient = new SemanticBaseUnitClient(this)

  /** Contains functionality associated with specific elements of the AMF model */
  override def elementClient(): ShapesElementClient = new ShapesElementClient(this)

  /** Contains methods to get information about the current state of the configuration */
  override def configurationState(): AMLConfigurationState = new AMLConfigurationState(this)

  /** Set [[ParsingOptions]]
    * @param parsingOptions
    *   [[ParsingOptions]] to add to configuration object
    * @return
    *   [[ShapesConfiguration]] with [[ParsingOptions]] added
    */
  override def withParsingOptions(parsingOptions: ParsingOptions): SemanticJsonSchemaConfiguration =
    super._withParsingOptions(parsingOptions)

  /** Set [[RenderOptions]]
    * @param renderOptions
    *   [[RenderOptions]] to add to configuration object
    * @return
    *   [[ShapesConfiguration]] with [[ParsingOptions]] added
    */
  override def withRenderOptions(renderOptions: RenderOptions): SemanticJsonSchemaConfiguration =
    super._withRenderOptions(renderOptions)

  /** Add a [[ResourceLoader]]
    * @param rl
    *   [[ResourceLoader]] to add to configuration object
    * @return
    *   [[ShapesConfiguration]] with the [[ResourceLoader]] added
    */
  override def withResourceLoader(rl: ResourceLoader): SemanticJsonSchemaConfiguration =
    super._withResourceLoader(rl)

  /** Set the configuration [[ResourceLoader]]s
    * @param rl
    *   a list of [[ResourceLoader]] to set to the configuration object
    * @return
    *   [[ShapesConfiguration]] with [[ResourceLoader]]s set
    */
  override def withResourceLoaders(rl: List[ResourceLoader]): SemanticJsonSchemaConfiguration =
    super._withResourceLoaders(rl)

  /** Set [[UnitCache]]
    * @param cache
    *   [[UnitCache]] to add to configuration object
    * @return
    *   [[ShapesConfiguration]] with [[UnitCache]] added
    */
  override def withUnitCache(cache: UnitCache): SemanticJsonSchemaConfiguration =
    super._withUnitCache(cache)

  override def withFallback(plugin: DomainParsingFallback): SemanticJsonSchemaConfiguration =
    super._withFallback(plugin)

  override def withPlugin(amfPlugin: AMFPlugin[_]): SemanticJsonSchemaConfiguration =
    super._withPlugin(amfPlugin)

  override def withRootParsePlugin(amfParsePlugin: AMFParsePlugin): SemanticJsonSchemaConfiguration =
    super._withRootParsePlugin(amfParsePlugin)

  override def withReferenceParsePlugin(plugin: AMFParsePlugin): ShapesConfiguration =
    super._withReferenceParsePlugin(plugin)

  override def withPlugins(plugins: List[AMFPlugin[_]]): SemanticJsonSchemaConfiguration =
    super._withPlugins(plugins)

  private[amf] override def withValidationProfile(profile: ValidationProfile): SemanticJsonSchemaConfiguration =
    super._withValidationProfile(profile)

  // Keep AMF internal, done to avoid recomputing validations every time a config is requested
  private[amf] override def withValidationProfile(
      profile: ValidationProfile,
      effective: EffectiveValidations
  ): SemanticJsonSchemaConfiguration =
    super._withValidationProfile(profile, effective)

  /** Add a [[TransformationPipeline]]
    * @param pipeline
    *   [[TransformationPipeline]] to add to configuration object
    * @return
    *   [[ShapesConfiguration]] with [[TransformationPipeline]] added
    */
  override def withTransformationPipeline(pipeline: TransformationPipeline): SemanticJsonSchemaConfiguration =
    super._withTransformationPipeline(pipeline)

  /** AMF internal method just to facilitate the construction */
  override private[amf] def withTransformationPipelines(
      pipelines: List[TransformationPipeline]
  ): SemanticJsonSchemaConfiguration =
    super._withTransformationPipelines(pipelines)

  /** Set [[ErrorHandlerProvider]]
    * @param provider
    *   [[ErrorHandlerProvider]] to set to configuration object
    * @return
    *   [[ShapesConfiguration]] with [[ErrorHandlerProvider]] set
    */
  override def withErrorHandlerProvider(provider: ErrorHandlerProvider): SemanticJsonSchemaConfiguration =
    super._withErrorHandlerProvider(provider)

  /** Add an [[AMFEventListener]]
    * @param listener
    *   [[AMFEventListener]] to add to configuration object
    * @return
    *   [[ShapesConfiguration]] with [[AMFEventListener]] added
    */
  override def withEventListener(listener: AMFEventListener): SemanticJsonSchemaConfiguration =
    super._withEventListener(listener)

  private[amf] override def withEntities(entities: Map[String, ModelDefaultBuilder]): SemanticJsonSchemaConfiguration =
    super._withEntities(entities)

  private[amf] override def withExtensions(dialect: Dialect): SemanticJsonSchemaConfiguration = {
    super.withExtensions(dialect).asInstanceOf[SemanticJsonSchemaConfiguration]
  }

  private[amf] override def withAnnotations(
      annotations: Map[String, AnnotationGraphLoader]
  ): SemanticJsonSchemaConfiguration =
    super._withAnnotations(annotations)

  /** Set [[BaseExecutionEnvironment]]
    * @param executionEnv
    *   [[BaseExecutionEnvironment]] to set to configuration object
    * @return
    *   [[ShapesConfiguration]] with [[BaseExecutionEnvironment]] set
    */
  override def withExecutionEnvironment(executionEnv: ExecutionEnvironment): SemanticJsonSchemaConfiguration =
    super._withExecutionEnvironment(executionEnv)

  /** Register a Dialect
    * @param dialect
    *   [[Dialect]] to register
    * @return
    *   [[ShapesConfiguration]] with [[Dialect]] registered
    */
  override def withDialect(dialect: Dialect): SemanticJsonSchemaConfiguration =
    super.withDialect(dialect).asInstanceOf[SemanticJsonSchemaConfiguration]

  /** Register a Dialect
    * @param url
    *   URL of the Dialect to register
    * @return
    *   A CompletableFuture of [[ShapesConfiguration]]
    */
  override def withDialect(url: String): Future[SemanticJsonSchemaConfiguration] =
    super.withDialect(url).map(_.asInstanceOf[SemanticJsonSchemaConfiguration])(getExecutionContext)

  /** Register a [[Dialect]] linked from a [[DialectInstance]]
    * @param url
    *   of the [[DialectInstance]]
    * @return
    *   A CompletableFuture of [[ShapesConfiguration]]
    */
  override def forInstance(url: String): Future[SemanticJsonSchemaConfiguration] =
    super.forInstance(url).map(_.asInstanceOf[SemanticJsonSchemaConfiguration])(getExecutionContext)
}

object SemanticJsonSchemaConfiguration {

  def empty(): SemanticJsonSchemaConfiguration = {
    new SemanticJsonSchemaConfiguration(
      AMFResolvers.predefined(),
      DefaultErrorHandlerProvider,
      AMLRegistry.empty,
      Set.empty,
      AMFOptions.default()
    )
  }

  def predefined(): SemanticJsonSchemaConfiguration = {
    val shapesConfig = ShapesConfiguration.predefined()
    new SemanticJsonSchemaConfiguration(
      shapesConfig.resolvers,
      shapesConfig.errorHandlerProvider,
      shapesConfig.registry,
      shapesConfig.listeners,
      shapesConfig.options
    ).withPlugin(JsonSchemaDialectParsePlugin)
  }
}
