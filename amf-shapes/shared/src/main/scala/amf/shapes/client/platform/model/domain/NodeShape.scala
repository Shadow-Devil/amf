package amf.shapes.client.platform.model.domain

import amf.core.client.platform.model.{BoolField, IntField, StrField}
import amf.core.client.platform.model.domain.{PropertyShape, Shape}
import amf.shapes.client.platform.model.domain.federation.ExternalPropertyShape
import amf.shapes.client.scala.model.domain.{NodeShape => InternalNodeShape}
import amf.shapes.internal.convert.ShapeClientConverters.ClientList

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import amf.shapes.internal.convert.ShapeClientConverters._

@JSExportAll
case class NodeShape(override private[amf] val _internal: InternalNodeShape) extends AnyShape(_internal) {

  @JSExportTopLevel("NodeShape")
  def this() = this(InternalNodeShape())

  def isAbstract: BoolField                                            = _internal.isAbstract
  def isInputOnly: BoolField                                           = _internal.isInputOnly
  def minProperties: IntField                                          = _internal.minProperties
  def maxProperties: IntField                                          = _internal.maxProperties
  def closed: BoolField                                                = _internal.closed
  def discriminator: StrField                                          = _internal.discriminator
  def discriminatorValue: StrField                                     = _internal.discriminatorValue
  def discriminatorMapping: ClientList[IriTemplateMapping]             = _internal.discriminatorMapping.asClient
  def discriminatorValueMapping: ClientList[DiscriminatorValueMapping] = _internal.discriminatorValueMapping.asClient
  def properties: ClientList[PropertyShape]                            = _internal.properties.asClient
  def additionalPropertiesSchema: Shape                                = _internal.additionalPropertiesSchema
  def additionalPropertiesKeySchema: Shape                             = _internal.additionalPropertiesKeySchema
  def dependencies: ClientList[PropertyDependencies]                   = _internal.dependencies.asClient
  def schemaDependencies: ClientList[SchemaDependencies]               = _internal.schemaDependencies.asClient
  def propertyNames: Shape                                             = _internal.propertyNames
  def unevaluatedProperties: Boolean                                   = _internal.unevaluatedProperties
  def unevaluatedPropertiesSchema: Shape                               = _internal.unevaluatedPropertiesSchema
  def keys: ClientList[federation.Key]                                 = _internal.keys.asClient
  def externalProperties: ClientList[ExternalPropertyShape]            = _internal.externalProperties.asClient

  def withIsAbstract(isAbstract: Boolean): this.type = {
    _internal.withIsAbstract(isAbstract)
    this
  }

  def withIsInputOnly(isInputOnly: Boolean): this.type = {
    _internal.withIsInputOnly(isInputOnly)
    this
  }

  def withMinProperties(min: Int): this.type = {
    _internal.withMinProperties(min)
    this
  }
  def withMaxProperties(max: Int): this.type = {
    _internal.withMaxProperties(max)
    this
  }
  def withClosed(closed: Boolean): this.type = {
    _internal.withClosed(closed)
    this
  }
  def withDiscriminator(discriminator: String): this.type = {
    _internal.withDiscriminator(discriminator)
    this
  }
  def withDiscriminatorValue(value: String): this.type = {
    _internal.withDiscriminatorValue(value)
    this
  }
  def withDiscriminatorMapping(mappings: ClientList[IriTemplateMapping]): this.type = {
    _internal.withDiscriminatorMapping(mappings.asInternal)
    this
  }
  def withProperties(properties: ClientList[PropertyShape]): this.type = {
    _internal.withProperties(properties.asInternal)
    this
  }
  def withAdditionalPropertiesSchema(additionalPropertiesSchema: Shape): this.type = {
    _internal.withAdditionalPropertiesSchema(additionalPropertiesSchema)
    this
  }
  def withAdditionalPropertiesKeySchema(additionalPropertiesKeySchema: Shape): this.type = {
    _internal.withAdditionalPropertiesKeySchema(additionalPropertiesKeySchema)
    this
  }
  def withDependencies(dependencies: ClientList[PropertyDependencies]): this.type = {
    _internal.withDependencies(dependencies.asInternal)
    this
  }
  def withSchemaDependencies(dependencies: ClientList[SchemaDependencies]): this.type = {
    _internal.withSchemaDependencies(dependencies.asInternal)
    this
  }
  def withPropertyNames(propertyNames: Shape): this.type = {
    _internal.withPropertyNames(propertyNames)
    this
  }
  def withUnevaluatedProperties(value: Boolean): this.type = {
    _internal.withUnevaluatedProperties(value)
    this
  }

  def withUnevaluatedPropertiesSchema(schema: Shape): this.type = {
    _internal.withUnevaluatedPropertiesSchema(schema)
    this
  }

  def withProperty(name: String): PropertyShape = _internal.withProperty(name)

  def withDependency(): PropertyDependencies = _internal.withDependency()

  def withInheritsObject(name: String): NodeShape = _internal.withInheritsObject(name)

  def withInheritsScalar(name: String): ScalarShape = _internal.withInheritsScalar(name)

  def withKeys(keys: ClientList[federation.Key]): this.type = {
    _internal.withKeys(keys.asInternal)
    this
  }

  def withExternalProperties(externalProperties: ClientList[ExternalPropertyShape]): this.type = {
    _internal.withExternalProperties(externalProperties.asInternal)
    this
  }

  override def linkCopy(): NodeShape = _internal.linkCopy()
}
