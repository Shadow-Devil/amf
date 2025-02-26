package amf.apicontract.internal.spec.oas.parser.document

import amf.apicontract.client.scala.model.document._
import amf.apicontract.client.scala.model.domain.templates.{ResourceType, Trait}
import amf.apicontract.internal.plugins.ApiContractFallbackPlugin
import amf.apicontract.internal.spec.common.parser.{AbstractDeclarationParser, WebApiLikeReferencesParser}
import amf.apicontract.internal.spec.oas.OasHeader
import amf.apicontract.internal.spec.oas.OasHeader._
import amf.apicontract.internal.spec.oas.parser.context.OasWebApiContext
import amf.core.client.scala.model.document.{BaseUnit, ExternalFragment}
import amf.core.client.scala.model.domain.extensions.CustomDomainProperty
import amf.core.client.scala.model.domain.{ExternalDomainElement, Shape}
import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.internal.parser.Root
import amf.core.internal.parser.domain.{Annotations, ScalarNode}
import amf.core.internal.remote.Spec
import amf.core.internal.unsafe.PlatformSecrets
import amf.core.internal.utils._
import amf.shapes.client.scala.model.domain.Example
import amf.shapes.internal.spec.common.parser.{
  ExampleOptions,
  OasLikeCreativeWorkParser,
  RamlNamedExampleParser,
  YMapEntryLike
}
import amf.shapes.internal.spec.common.{OAS20SchemaVersion, SchemaPosition}
import amf.shapes.internal.spec.oas.parser.OasTypeParser
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.InvalidFragmentType
import org.yaml.model.{YMap, YMapEntry, YScalar}

case class OasFragmentParser(root: Root, spec: Spec, fragment: Option[OasHeader] = None)(implicit
    val ctx: OasWebApiContext
) extends OasSpecParser()
    with PlatformSecrets {

  def parseFragment(): BaseUnit = {
    // first i must identify the type of fragment
    val parsed = root.parsed.asInstanceOf[SyamlParsedDocument]
    val map: YMap = parsed.document.to[YMap] match {
      case Right(m) => m
      case _ =>
        ctx.eh.violation(
          InvalidFragmentType,
          root.location,
          "Cannot parse empty map",
          root.parsed.asInstanceOf[SyamlParsedDocument].document.location
        )
        YMap.empty
    }

    val fragment = (detectType() flatMap {
      case Oas20DocumentationItem         => Some(DocumentationItemFragmentParser(map).parse())
      case Oas20DataType                  => Some(DataTypeFragmentParser(map).parse())
      case Oas20ResourceType              => Some(ResourceTypeFragmentParser(map).parse())
      case Oas20Trait                     => Some(TraitFragmentParser(map).parse())
      case Oas20AnnotationTypeDeclaration => Some(AnnotationFragmentParser(map).parse())
      case Oas20SecurityScheme            => Some(SecuritySchemeFragmentParser(map).parse())
      case Oas20NamedExample              => Some(NamedExampleFragmentParser(map).parse())
      case Oas20Header | Oas30Header =>
        Some(ApiContractFallbackPlugin(false, skipValidations = true).plugin(parsed).parse(root, ctx))
      case _ => None
    }).getOrElse {
      val fragment = ExternalFragment()
        .withLocation(root.location)
        .withId(root.location)
        .withEncodes(ExternalDomainElement().withRaw(root.raw))
      ctx.eh.violation(InvalidFragmentType, fragment, "Unsupported oas type", map.location)
      fragment
    }

    val references =
      WebApiLikeReferencesParser(fragment, root.location, "uses".asOasExtension, map, root.references).parse()

    fragment
      .withLocation(root.location)
      .add(Annotations(root.parsed.asInstanceOf[SyamlParsedDocument].document))
      .withProcessingData(APIContractProcessingData().withSourceSpec(spec))

    UsageParser(map, fragment).parse()

    if (references.nonEmpty) fragment.withReferences(references.baseUnitReferences())
    fragment
  }

  def detectType(): Option[OasHeader] = fragment.orElse(OasHeader(root))

  case class DocumentationItemFragmentParser(map: YMap) {
    def parse(): DocumentationItemFragment = {

      val item = DocumentationItemFragment()

      item.withEncodes(OasLikeCreativeWorkParser(map, item.id).parse())

      item
    }
  }

  case class DataTypeFragmentParser(map: YMap) {
    def parse(): DataTypeFragment = {
      val dataType = DataTypeFragment()
      val filterMap = YMap(
        map.entries.filter({
          case c: YMapEntry =>
            val key = ScalarNode(c.key).text().toString
            !key.equals(Oas20DataType.key) && !key.equals(OasHeader.swagger)
          case _ => true
        }),
        map.sourceName
      )

      val shapeOption =
        OasTypeParser(
          YMapEntryLike(filterMap),
          "type",
          (shape: Shape) => shape.withId(root.location + "#/shape"),
          OAS20SchemaVersion(SchemaPosition.Schema)
        )
          .parse()
      shapeOption.map(dataType.withEncodes(_))

      dataType
      //
    }
  }

  case class AnnotationFragmentParser(map: YMap) {
    def parse(): AnnotationTypeDeclarationFragment = {
      val annotation = AnnotationTypeDeclarationFragment()

      val property =
        AnnotationTypesParser(map, "annotation", map, (annotation: CustomDomainProperty) => Unit).parse()

      annotation.withEncodes(property)
    }
  }

  case class ResourceTypeFragmentParser(map: YMap) {
    def parse(): ResourceTypeFragment = {
      val resourceType = ResourceTypeFragment()

      val abstractDeclaration =
        new AbstractDeclarationParser(
          ResourceType(map).withId(resourceType.id + "#/"),
          resourceType.id,
          YMapEntryLike(map)
        ).parse()

      resourceType.withEncodes(abstractDeclaration)

    }
  }

  case class TraitFragmentParser(map: YMap) {
    def parse(): TraitFragment = {
      val traitFragment = TraitFragment()

      val abstractDeclaration =
        new AbstractDeclarationParser(Trait(map).withId(traitFragment.id + "#/"), traitFragment.id, YMapEntryLike(map))
          .parse()

      traitFragment.withEncodes(abstractDeclaration)
    }
  }

  case class SecuritySchemeFragmentParser(map: YMap) {
    def parse(): SecuritySchemeFragment = {
      val security = SecuritySchemeFragment()

      security.withEncodes(
        ctx.factory
          .securitySchemeParser(
            map,
            (security: amf.apicontract.client.scala.model.domain.security.SecurityScheme) => security
          )
          .parse()
      )
    }
  }

  case class NamedExampleFragmentParser(map: YMap) {
    def parse(): NamedExampleFragment = {
      val entries      = map.entries.filter(e => e.key.as[YScalar].text != "fragmentType".asOasExtension)
      val namedExample = NamedExampleFragment()

      val producer = (name: Option[String]) => {
        val example = Example()
        name.foreach(example.withName(_))
        namedExample.withEncodes(example)
        example
      }

      namedExample.withEncodes(
        RamlNamedExampleParser(entries.head, producer, ExampleOptions(strictDefault = true, quiet = true)).parse()
      )
    }
  }
}
