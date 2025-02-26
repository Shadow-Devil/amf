package amf.shapes.internal.spec.common.parser

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar, Annotation, DataNode}
import amf.core.internal.annotations.LexicalInformation
import amf.core.internal.datanode.DataNodeParser
import amf.core.internal.errorhandling.WarningOnlyHandler
import amf.core.internal.parser.domain._
import amf.core.internal.parser.{YMapOps, YNodeLikeOps}
import amf.core.internal.validation.CoreValidations
import amf.shapes.client.scala.model.domain.{AnyShape, Example, ExemplifiedDomainElement, ScalarShape}
import amf.shapes.internal.annotations.{ExternalReferenceUrl, ParsedJSONExample}
import amf.shapes.internal.domain.metamodel.ExampleModel
import amf.shapes.internal.domain.metamodel.common.ExamplesField
import amf.shapes.internal.spec.RamlTypeDefMatcher.{JSONSchema, XMLSchema}
import amf.shapes.internal.spec.common._
import amf.shapes.internal.spec.oas.OasShapeDefinitions
import amf.shapes.internal.spec.raml.parser.RamlWebApiContextType
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.{
  ExamplesMustBeAMap,
  ExclusivePropertiesSpecification,
  InvalidFragmentType
}
import amf.shapes.internal.vocabulary.VocabularyMappings
import org.mulesoft.common.client.lexical.Position
import org.yaml.model._
import org.yaml.parser.JsonParser

import scala.collection.mutable.ListBuffer

case class OasExamplesParser(map: YMap, exemplifiedDomainElement: ExemplifiedDomainElement)(implicit
    ctx: ShapeParserContext
) {
  def parse(): Unit = {
    (map.key("example"), map.key("examples")) match {
      case (Some(exampleEntry), None) =>
        val examples = List(parseExample(exampleEntry.value))
        exemplifiedDomainElement.setWithoutId(
          ExamplesField.Examples,
          AmfArray(examples, Annotations(exampleEntry)),
          Annotations(exampleEntry)
        )
      case (None, Some(examplesEntry)) =>
        val examples = Oas3NamedExamplesParser(examplesEntry, exemplifiedDomainElement.id).parse()
        exemplifiedDomainElement.setWithoutId(
          ExamplesField.Examples,
          AmfArray(examples, Annotations(examplesEntry.value)),
          Annotations(examplesEntry)
        )
      case (Some(_), Some(_)) =>
        ctx.eh.violation(
          ExclusivePropertiesSpecification,
          exemplifiedDomainElement,
          s"Properties 'example' and 'examples' are exclusive and cannot be declared together",
          map.location
        )
      case _ => // ignore
    }
  }

  private def parseExample(yNode: YNode) = {
    val example = Example(yNode)
    ExampleDataParser(YMapEntryLike(yNode), example, Oas3ExampleOptions).parse()
  }
}

case class Oas3NamedExamplesParser(entry: YMapEntry, parentId: String)(implicit ctx: ShapeParserContext) {
  def parse(): Seq[Example] = {
    entry.value
      .as[YMap]
      .entries
      .map(e => Oas3NameExampleParser(e, parentId, Oas3ExampleOptions).parse())
  }
}

case class RamlExamplesParser(
    map: YMap,
    singleExampleKey: String,
    multipleExamplesKey: String,
    exemplified: ExemplifiedDomainElement,
    options: ExampleOptions
)(implicit ctx: ShapeParserContext) {
  def parse(): Unit = {
    if (map.key(singleExampleKey).isDefined && map.key(multipleExamplesKey).isDefined) {
      ctx.eh.violation(
        ExclusivePropertiesSpecification,
        exemplified,
        s"Properties '$singleExampleKey' and '$multipleExamplesKey' are exclusive and cannot be declared together",
        map.location
      )
    }
    val examples = RamlMultipleExampleParser(multipleExamplesKey, map, exemplified.withExample, options).parse() ++
      RamlSingleExampleParser(singleExampleKey, map, exemplified.withExample, options).parse()

    map
      .key(multipleExamplesKey)
      .orElse(map.key(singleExampleKey)) match {
      case Some(e) =>
        exemplified.setWithoutId(ExamplesField.Examples, AmfArray(examples, Annotations(e)), Annotations(e))
      case _ if examples.nonEmpty =>
        exemplified.setWithoutId(ExamplesField.Examples, AmfArray(examples), Annotations.inferred())
      case _ => // ignore
    }
  }
}

case class RamlMultipleExampleParser(
    key: String,
    map: YMap,
    producer: Option[String] => Example,
    options: ExampleOptions
)(implicit ctx: ShapeParserContext) {
  def parse(): Seq[Example] = {
    val examples = ListBuffer[Example]()

    map.key(key).foreach { entry =>
      ctx.link(entry.value) match {
        case Left(s) =>
          examples += ctx
            .findNamedExampleOrError(entry.value)(s)
            .link(s)

        case Right(node) =>
          node.tagType match {
            case YType.Map =>
              examples ++= node.as[YMap].entries.map(RamlNamedExampleParser(_, producer, options).parse())
            case YType.Null => // ignore
            case YType.Str
                if node.toString().matches("<<.*>>") && ctx.isRamlContext
                  && !ctx.ramlContextType.contains(RamlWebApiContextType.DEFAULT) => // Ignore
            case _ =>
              ctx.eh.violation(
                ExamplesMustBeAMap,
                "",
                s"Property '$key' should be a map",
                entry.location
              )
          }
      }
    }
    examples
  }
}

case class RamlNamedExampleParser(entry: YMapEntry, producer: Option[String] => Example, options: ExampleOptions)(
    implicit ctx: ShapeParserContext
) {
  def parse(): Example = {
    val name           = ScalarNode(entry.key)
    val simpleProducer = () => producer(Some(name.text().toString))
    val example: Example = ctx.link(entry.value) match {
      case Left(s) =>
        ctx
          .findNamedExample(s)
          .map(e => e.link(ScalarNode(entry.value), Annotations(entry)).asInstanceOf[Example])
          .getOrElse(RamlSingleExampleValueParser(entry, simpleProducer, options).parse())
      case Right(_) => RamlSingleExampleValueParser(entry, simpleProducer, options).parse()
    }
    example.setWithoutId(ExampleModel.Name, name.text(), Annotations.inferred())
  }
}

case class RamlSingleExampleParser(
    key: String,
    map: YMap,
    producer: Option[String] => Example,
    options: ExampleOptions
)(implicit ctx: ShapeParserContext) {
  def parse(): Option[Example] = {
    val newProducer = () => producer(None)
    map.key(key).flatMap { entry =>
      ctx.link(entry.value) match {
        case Left(s) =>
          ctx
            .findNamedExample(
              s,
              Some(errMsg =>
                ctx.eh.violation(
                  InvalidFragmentType,
                  "",
                  errMsg,
                  entry.value.location
                )
              )
            )
            .map(e => e.link(ScalarNode(entry.value), Annotations(entry)).asInstanceOf[Example])
        case Right(node) =>
          node.tagType match {
            case YType.Map =>
              Option(RamlSingleExampleValueParser(entry, newProducer, options).parse())
            case YType.Null => checkEmptyExample(newProducer, entry, node)
            case _ => // example can be any type or scalar value, like string int datetime etc. We will handle all like strings in this stage
              parseExample(newProducer, entry, node)
          }
      }
    }
  }

  private def checkEmptyExample(newProducer: () => Example, entry: YMapEntry, node: YNode): Option[Example] = {
    node.value match {
      case value: YScalar if value.text.nonEmpty => parseExample(newProducer, entry, node)
      case _                                     => None
    }
  }

  private def parseExample(newProducer: () => Example, entry: YMapEntry, node: YNode): Option[Example] = {
    Option(
      ExampleDataParser(YMapEntryLike(node), newProducer().add(Annotations(entry.value)), options)
        .parse()
    )
  }
}

case class RamlSingleExampleValueParser(entry: YMapEntry, producer: () => Example, options: ExampleOptions)(implicit
    ctx: ShapeParserContext
) extends QuickFieldParserOps {
  def parse(): Example = {
    val example = producer().add(Annotations(entry))

    entry.value.tagType match {
      case YType.Map =>
        val map = entry.value.as[YMap]

        if (map.key("value").nonEmpty) {
          map.key("displayName", (ExampleModel.DisplayName in example).allowingAnnotations)
          map.key("description", (ExampleModel.Description in example).allowingAnnotations)
          map.key("strict", (ExampleModel.Strict in example).allowingAnnotations)

          map
            .key("value")
            .foreach { entry =>
              ExampleDataParser(YMapEntryLike(entry), example, options).parse()
            }

          AnnotationParser(example, map, List(VocabularyMappings.example)).parse()

          if (ctx.spec.isRaml) ctx.closedShape(example, map, "example")
        } else ExampleDataParser(YMapEntryLike(entry.value), example, options).parse()
      case YType.Null => // ignore
      case _          => ExampleDataParser(YMapEntryLike(entry.value), example, options).parse()
    }

    example
  }
}

case class Oas3NameExampleParser(entry: YMapEntry, parentId: String, options: ExampleOptions)(implicit
    ctx: ShapeParserContext
) extends QuickFieldParserOps {
  def parse(): Example = {
    val map = entry.value.as[YMap]

    ctx.link(map) match {
      case Left(fullRef) => parseLink(fullRef, map).add(Annotations(entry))
      case Right(_)      => Oas3ExampleValueParser(map, newExample(map), options).parse()
    }
  }

  private val keyName = ScalarNode(entry.key)

  private def setName(e: Example): Example =
    e.setWithoutId(ExampleModel.Name, keyName.string(), Annotations(entry.key))

  private def newExample(ast: YPart): Example =
    setName(Example(entry))

  private def parseLink(fullRef: String, map: YMap) = {
    val name = OasShapeDefinitions.stripOas3ComponentsPrefix(fullRef, "examples")
    ctx
      .findExample(name, SearchScope.All)
      .map(found => setName(found.link(AmfScalar(name), Annotations(map), Annotations.synthesized())))
      .getOrElse {
        ctx.obtainRemoteYNode(fullRef) match {
          case Some(exampleNode) =>
            Oas3ExampleValueParser(exampleNode.as[YMap], newExample(exampleNode), options)
              .parse()
              .add(ExternalReferenceUrl(fullRef))
          case None =>
            ctx.eh.violation(
              CoreValidations.UnresolvedReference,
              "",
              s"Cannot find example reference $fullRef",
              map.location
            )
            val errorExample =
              setName(
                error.ErrorNamedExample(name, map).link(AmfScalar(name), Annotations(map), Annotations.synthesized())
              )

            errorExample
        }
      }
  }
}

case class Oas3ExampleValueParser(map: YMap, example: Example, options: ExampleOptions)(implicit
    ctx: ShapeParserContext
) extends QuickFieldParserOps {
  def parse(): Example = {
    map.key("summary", (ExampleModel.Summary in example).allowingAnnotations)
    map.key("description", (ExampleModel.Description in example).allowingAnnotations)
    map.key("externalValue", (ExampleModel.ExternalValue in example).allowingAnnotations)

    example.setWithoutId(ExampleModel.Strict, AmfScalar(options.strictDefault), Annotations.synthesized())

    map
      .key("value")
      .foreach { entry =>
        ExampleDataParser(YMapEntryLike(entry), example, options).parse()
      }

    AnnotationParser(example, map, List(VocabularyMappings.example)).parse()

    ctx.closedShape(example, map, "example")
    example
  }
}

case class NodeDataNodeParser(
    node: YNode,
    parentId: String,
    quiet: Boolean,
    fromExternal: Boolean = false,
    isScalar: Boolean = false
)(implicit ctx: ShapeParserContext) {
  def parse(): DataNodeParserResult = {
    val errorHandler             = if (quiet) WarningOnlyHandler(ctx.eh) else ctx.eh
    var jsonText: Option[String] = None
    val exampleNode: Option[YNode] = node.toOption[YScalar] match {
      case Some(scalar) if scalar.mark.isInstanceOf[QuotedMark] => Some(node)
      case Some(_) if isScalar                                  => Some(node)
      case Some(scalar) if JSONSchema.unapply(scalar.text).isDefined =>
        jsonText = Some(scalar.text)
        Some(
          ExternalFragmentHelper.searchForAlreadyParsedNodeInFragments(node).getOrElse {
            jsonParser(scalar, errorHandler).document().node
          }
        )
      case Some(scalar) if XMLSchema.unapply(scalar.text).isDefined => None
      case _                                                        => Some(node) // return default node for xml too.
    }

    errorHandler match {
      case wh: WarningOnlyHandler if wh.hasRegister && jsonText.isDefined =>
        parseDataNode(exampleNode, jsonText.map(ParsedJSONExample(_)).toSeq)
      case wh: WarningOnlyHandler if wh.hasRegister => DataNodeParserResult(exampleNode, None)
      case _ => parseDataNode(exampleNode, jsonText.map(ParsedJSONExample(_)).toSeq)

    }
  }

  private def jsonParser(scalar: YScalar, errorHandler: AMFErrorHandler): JsonParser =
    if (fromExternal)
      JsonParserFactory.fromCharsWithSource(scalar.text, scalar.sourceName, ctx.options.getMaxJsonYamlDepth)(ctx.eh)
    else
      JsonParserFactory.fromCharsWithSource(
        scalar.text,
        scalar.sourceName,
        Position(node.range.lineFrom, node.range.columnFrom),
        ctx.options.getMaxJsonYamlDepth
      )(errorHandler)

  private def parseDataNode(exampleNode: Option[YNode], ann: Seq[Annotation] = Seq()) = {
    val dataNode = exampleNode.map { ex =>
      val dataNode = DataNodeParser(ex).parse()
      dataNode.annotations.reject(_.isInstanceOf[LexicalInformation])
      dataNode.annotations += LexicalInformation(ex.value.range)
      ann.foreach { a =>
        dataNode.annotations += a
      }

      dataNode
    }
    DataNodeParserResult(exampleNode, dataNode)
  }
}

case class DataNodeParserResult(exampleNode: Option[YNode], dataNode: Option[DataNode]) {}

case class ExampleOptions(strictDefault: Boolean, quiet: Boolean, isScalar: Boolean = false) {
  def checkScalar(shape: AnyShape): ExampleOptions = shape match {
    case _: ScalarShape =>
      ExampleOptions(strictDefault, quiet, isScalar = true)
    case _ => this
  }
}

object DefaultExampleOptions extends ExampleOptions(true, false, false)

object Oas3ExampleOptions extends ExampleOptions(strictDefault = true, quiet = true)
