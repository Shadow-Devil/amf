package amf.apicontract.internal.spec.oas

import amf.apicontract.internal.plugins.ApiParsePlugin
import amf.apicontract.internal.spec.oas.parser.context.OasLikeWebApiContext
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.document.DefaultReferenceCollector

trait OasLikeParsePlugin extends ApiParsePlugin {

  protected def promoteFragments(unit: BaseUnit, ctx: OasLikeWebApiContext): BaseUnit = {
    val collector = DefaultReferenceCollector[BaseUnit]()
    unit.references.foreach(baseUnit => collector += (baseUnit.location().getOrElse(baseUnit.id), baseUnit))
    ctx.declarations.promotedFragments.foreach { promoted =>
      val key = promoted.location().getOrElse(promoted.id)
      collector += (key, promoted)
    }
    if (collector.nonEmpty) unit.withReferences(collector.references())
    else unit
  }
}
