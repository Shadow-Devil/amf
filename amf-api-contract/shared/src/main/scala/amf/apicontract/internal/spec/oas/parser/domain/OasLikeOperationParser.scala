package amf.apicontract.internal.spec.oas.parser.domain

import amf.apicontract.client.scala.model.domain.security.SecurityRequirement
import amf.apicontract.client.scala.model.domain.{Operation, Request, Response}
import amf.apicontract.internal.metamodel.domain.OperationModel.Method
import amf.apicontract.internal.metamodel.domain.api.WebApiModel
import amf.apicontract.internal.metamodel.domain.{OperationModel, ResponseModel}
import amf.apicontract.internal.spec.common.parser.{OasLikeSecurityRequirementParser, SpecParserOps}
import amf.apicontract.internal.spec.oas.parser.context.{Oas3WebApiContext, OasLikeWebApiContext, OasWebApiContext}
import amf.apicontract.internal.spec.raml.parser.domain.ParametrizedDeclarationParser
import amf.apicontract.internal.validation.definitions.ParserSideValidations.{DuplicatedOperationId, InvalidStatusCode}
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar}
import amf.core.internal.annotations.{SourceAST, VirtualElement}
import amf.core.internal.metamodel.domain.DomainElementModel
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, ScalarNode}
import amf.core.internal.utils.{AmfStrings, IdCounter}
import amf.shapes.internal.spec.common.parser.WellKnownAnnotation.isOasAnnotation
import amf.shapes.internal.spec.common.parser.{AnnotationParser, OasLikeCreativeWorkParser}
import org.yaml.model._

import scala.collection.mutable

abstract class OasLikeOperationParser(entry: YMapEntry, adopt: Operation => Operation)(implicit
    val ctx: OasLikeWebApiContext
) extends SpecParserOps {

  protected def entryKey: AmfScalar = ScalarNode(entry.key).string()

  protected val closedShapeName = "operation"

  def parse(): Operation = {
    val operation: Operation = Operation(Annotations(entry))
    operation.setWithoutId(Method, entryKey, Annotations.inferred())
    adopt(operation)

    val map = entry.value.as[YMap]

    ctx.closedShape(operation, map, closedShapeName)

    map.key("operationId").foreach { entry =>
      val operationId = entry.value.toString()
      if (!ctx.registerOperationId(operationId))
        ctx.eh
          .violation(DuplicatedOperationId, operation, s"Duplicated operation id '$operationId'", entry.value.location)
    }

    parseOperationId(map, operation)

    map.key("description", OperationModel.Description in operation)
    map.key("summary", OperationModel.Summary in operation)
    map.key(
      "externalDocs",
      OperationModel.Documentation in operation using (OasLikeCreativeWorkParser.parse(_, operation.id))
    )

    AnnotationParser(operation, map).parseOrphanNode("responses")
    AnnotationParser(operation, map).parse()

    operation
  }

  def parseOperationId(map: YMap, operation: Operation): Unit = {
    map.key("operationId", OperationModel.Name in operation)
    map.key("operationId", OperationModel.OperationId in operation)
  }
}

abstract class OasOperationParser(entry: YMapEntry, adopt: Operation => Operation)(
    override implicit val ctx: OasWebApiContext
) extends OasLikeOperationParser(entry, adopt) {
  override def parse(): Operation = {
    val operation = super.parse()
    val map       = entry.value.as[YMap]

    ctx.closedShape(operation, map, "operation")

    map.key("deprecated", OperationModel.Deprecated in operation)

    map.key(
      "tags",
      entry => {
        val tags = StringTagsParser(entry.value.as[YSequence], operation).parse()
        operation.setWithoutId(OperationModel.Tags, AmfArray(tags, Annotations(entry.value)), Annotations(entry))
      }
    )

    map.key(
      "is".asOasExtension,
      entry => {
        val traits = entry.value
          .as[Seq[YNode]]
          .map(value => {
            ParametrizedDeclarationParser(value, operation.withTrait, ctx.declarations.findTraitOrError(value))
              .parse()
          })
        if (traits.nonEmpty) operation.setArray(DomainElementModel.Extends, traits, Annotations(entry))
      }
    )

    map.key("security".asOasExtension, entry => { parseSecurity(operation, entry) })

    map.key("security", entry => { parseSecurity(operation, entry) })

    map.key(
      "responses",
      entry => {
        val responses = mutable.ListBuffer[Response]()

        entry.value
          .as[YMap]
          .entries
          .filter(y => !isOasAnnotation(y.key.as[YScalar].text))
          .foreach { entry =>
            val node = ScalarNode(entry.key)
            responses += OasResponseParser(
              entry.value.as[YMap],
              { r =>
                r.setWithoutId(ResponseModel.Name, node.text(), Annotations(entry.key))
                  .setWithoutId(ResponseModel.StatusCode, node.text(), Annotations.inferred())
                if (!r.annotations.contains(classOf[SourceAST]))
                  r.annotations ++= Annotations(entry)
                // Validation for OAS 3
                if (ctx.isInstanceOf[Oas3WebApiContext] && entry.key.tagType != YType.Str)
                  ctx.eh.violation(
                    InvalidStatusCode,
                    r,
                    "Status code for a Response object must be a string",
                    entry.key.location
                  )
              }
            ).parse()
          }

        operation.setWithoutId(
          OperationModel.Responses,
          AmfArray(responses, Annotations(entry.value)),
          Annotations(entry)
        )
      }
    )

    operation
  }

  private def parseSecurity(operation: Operation, entry: YMapEntry): Unit = {
    val idCounter = new IdCounter()
    // TODO check for empty array for resolution ?
    val requirements = entry.value
      .as[Seq[YNode]]
      .flatMap(s => OasLikeSecurityRequirementParser(s, (s: SecurityRequirement) => Unit, idCounter).parse())
    val extension = operation.security
    operation.setWithoutId(
      WebApiModel.Security,
      AmfArray(requirements ++ extension, Annotations(entry.value)),
      Annotations(entry)
    )
  }
}

case class Oas20OperationParser(entry: YMapEntry, adopt: Operation => Operation)(
    override implicit val ctx: OasWebApiContext
) extends OasOperationParser(entry, adopt) {
  override def parse(): Operation = {
    val operation = super.parse()
    val map       = entry.value.as[YMap]
    Oas20RequestParser(map, (r: Request) => Unit)
      .parse()
      .map(r =>
        operation.setWithoutId(OperationModel.Request, AmfArray(Seq(r), Annotations.virtual()), Annotations(map))
      )

    map.key("schemes", OperationModel.Schemes in operation)
    map.key("consumes", OperationModel.Accepts in operation)
    map.key("produces", OperationModel.ContentType in operation)

    operation
  }
}

case class Oas30OperationParser(entry: YMapEntry, adopt: Operation => Operation)(
    override implicit val ctx: OasWebApiContext
) extends OasOperationParser(entry, adopt) {
  override def parse(): Operation = {
    val operation = super.parse()
    val map       = entry.value.as[YMap]

    map.key(
      "requestBody",
      entry => {
        operation.fields.setWithoutId(
          OperationModel.Request,
          AmfArray(Seq(Oas30RequestParser(entry.value.as[YMap], operation.id, entry).parse()), Annotations.virtual()),
          Annotations.inferred()
        )
      }
    )

    // parameters defined in endpoint are stored in the request
    Oas30ParametersParser(map, Option(operation.request).map(() => _).getOrElse(operation.withInferredRequest))
      .parseParameters()

    map.key(
      "callbacks",
      entry => {
        val callbacks = entry.value
          .as[YMap]
          .entries
          .flatMap { callbackEntry =>
            val name = callbackEntry.key.as[YScalar].text
            Oas30CallbackParser(callbackEntry.value.as[YMap], _.withName(name), name, callbackEntry)
              .parse()
          }
        operation.fields
          .setWithoutId(OperationModel.Callbacks, AmfArray(callbacks, Annotations(entry.value)), Annotations(entry))
      }
    )

    ctx.factory.serversParser(map, operation).parse()

    operation
  }
}
