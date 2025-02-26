package amf.apicontract.internal.spec.raml.parser.document

import amf.apicontract.client.scala.model.document._
import amf.apicontract.client.scala.model.domain.templates.{ResourceType, Trait}
import amf.apicontract.internal.spec.common.parser.{AbstractDeclarationParser, ReferencesParserAnnotations}
import amf.apicontract.internal.spec.raml.RamlFragment
import amf.apicontract.internal.spec.raml.RamlFragmentHeader._
import amf.apicontract.internal.spec.raml.parser.context.RamlWebApiContext
import amf.apicontract.internal.spec.raml.parser.domain.RamlSecuritySchemeParser
import amf.core.client.scala.model.document.{ExternalFragment, Fragment}
import amf.core.client.scala.model.domain.extensions.CustomDomainProperty
import amf.core.client.scala.model.domain.{AmfScalar, ExternalDomainElement, Shape}
import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.internal.metamodel.document.FragmentModel
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.parser.{Root, YMapOps}
import amf.core.internal.remote.Spec
import amf.shapes.internal.spec.common.parser.{RamlCreativeWorkParser, YMapEntryLike}
import amf.shapes.internal.spec.raml.parser.{Raml10TypeParser, StringDefaultType}
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.InvalidFragmentType
import org.yaml.model.{YMap, YScalar}

/** */
case class RamlFragmentParser(root: Root, spec: Spec, fragmentType: RamlFragment)(implicit val ctx: RamlWebApiContext)
    extends RamlSpecParser {

  def parseFragment(): Fragment = {
    // first i must identify the type of fragment

    val rootMap: YMap = root.parsed.asInstanceOf[SyamlParsedDocument].document.to[YMap] match {
      case Right(map) => map
      case _          =>
        // we need to check if named example fragment in order to support invalid structures as external fragment
        if (fragmentType != Raml10NamedExample)
          ctx.eh.violation(
            InvalidFragmentType,
            root.location,
            "Cannot parse empty map",
            root.parsed.asInstanceOf[SyamlParsedDocument].document.location
          )
        YMap.empty
    }

    val (references, aliases) = ReferencesParserAnnotations("uses", rootMap, root)

    // usage is valid for a fragment, not for the encoded domain element
    val encodedMap = YMap(
      rootMap.entries.filter(e => e.key.as[YScalar].text != "usage" && e.key.as[YScalar].text != "uses"),
      root.location
    )

    val fragment = fragmentType match {
      case Raml10DocumentationItem         => DocumentationItemFragmentParser(encodedMap).parse()
      case Raml10DataType                  => DataTypeFragmentParser(encodedMap).parse()
      case Raml10ResourceType              => ResourceTypeFragmentParser(encodedMap).parse()
      case Raml10Trait                     => TraitFragmentParser(encodedMap).parse()
      case Raml10AnnotationTypeDeclaration => AnnotationFragmentParser(encodedMap).parse()
      case Raml10SecurityScheme            => SecuritySchemeFragmentParser(encodedMap).parse()
      case Raml10NamedExample              => NamedExampleFragmentParser(encodedMap).parse()
    }

    rootMap.key(
      "usage",
      usage => {
        fragment.setWithoutId(
          FragmentModel.Usage,
          AmfScalar(usage.value.as[String], Annotations(usage.value)),
          Annotations(usage.value)
        )
      }
    )
    fragment.withLocation(root.location).withProcessingData(APIContractProcessingData().withSourceSpec(spec))
    UsageParser(rootMap, fragment).parse()
    fragment.add(Annotations(root.parsed.asInstanceOf[SyamlParsedDocument].document))
    if (aliases.isDefined) fragment.annotations += aliases.get
    if (references.nonEmpty) fragment.withReferences(references.baseUnitReferences())
    fragment
  }

  private def buildExternalFragment(): ExternalFragment = {
    ExternalFragment()
      .withLocation(root.location)
      .withId(root.location)
      .withEncodes(
        ExternalDomainElement()
          .withRaw(root.raw)
          .withMediaType(root.mediatype)
      )
  }

  case class DocumentationItemFragmentParser(map: YMap) {
    def parse(): DocumentationItemFragment = {

      val item = DocumentationItemFragment()

      item.setWithoutId(
        FragmentModel.Encodes,
        RamlCreativeWorkParser(map).parse(),
        Annotations.inferred()
      )
    }
  }

  case class DataTypeFragmentParser(map: YMap) {
    def parse(): DataTypeFragment = {
      val dataType = DataTypeFragment()

      Raml10TypeParser(
        map,
        "type",
        (shape: Shape) => shape.withId(root.location + "#/shape"), // TODO: this is being ignored
        StringDefaultType
      )
        .parse()
        .foreach(dataType.setWithoutId(FragmentModel.Encodes, _, Annotations.inferred()))

      dataType
    }
  }

  case class ResourceTypeFragmentParser(map: YMap) {
    def parse(): ResourceTypeFragment = {
      val resourceType = ResourceTypeFragment()

      val abstractDeclaration =
        new AbstractDeclarationParser(ResourceType(map).withId(resourceType.id), resourceType.id, YMapEntryLike(map))
          .parse()

      resourceType.withEncodes(abstractDeclaration)
      resourceType.setWithoutId(FragmentModel.Encodes, abstractDeclaration, Annotations.inferred())
    }
  }

  case class TraitFragmentParser(map: YMap) {
    def parse(): TraitFragment = {
      val traitFragment = TraitFragment()

      val abstractDeclaration =
        new AbstractDeclarationParser(Trait(map).withId(traitFragment.id), traitFragment.id, YMapEntryLike(map))
          .parse()

      traitFragment.setWithoutId(FragmentModel.Encodes, abstractDeclaration, Annotations.inferred())
    }
  }

  case class AnnotationFragmentParser(map: YMap) {
    def parse(): AnnotationTypeDeclarationFragment = {
      val annotation = AnnotationTypeDeclarationFragment()

      val property =
        AnnotationTypesParser(map, "annotation", map, (annotation: CustomDomainProperty) => Unit).parse()

      annotation.setWithoutId(FragmentModel.Encodes, property, Annotations.inferred())
    }
  }

  case class SecuritySchemeFragmentParser(map: YMap) {
    def parse(): SecuritySchemeFragment = {
      val security = SecuritySchemeFragment()

      security.setWithoutId(
        FragmentModel.Encodes,
        RamlSecuritySchemeParser(
          map,
          (security: amf.apicontract.client.scala.model.domain.security.SecurityScheme) => security
        )
          .parse(),
        Annotations.inferred()
      )
    }
  }

  case class NamedExampleFragmentParser(map: YMap) {
    def parse(): Fragment = buildExternalFragment()
  }
}
