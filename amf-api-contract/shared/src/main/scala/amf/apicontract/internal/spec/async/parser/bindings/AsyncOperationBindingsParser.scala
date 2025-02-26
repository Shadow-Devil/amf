package amf.apicontract.internal.spec.async.parser.bindings

import amf.apicontract.client.scala.model.domain.bindings.amqp.Amqp091OperationBinding
import amf.apicontract.client.scala.model.domain.bindings.http.HttpOperationBinding
import amf.apicontract.client.scala.model.domain.bindings.kafka.KafkaOperationBinding
import amf.apicontract.client.scala.model.domain.bindings.mqtt.MqttOperationBinding
import amf.apicontract.client.scala.model.domain.bindings.{OperationBinding, OperationBindings}
import amf.apicontract.internal.metamodel.domain.bindings._
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.common.WebApiDeclarations.ErrorOperationBindings
import amf.apicontract.internal.spec.spec.OasDefinitions
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, SearchScope}
import amf.shapes.internal.spec.common.parser.YMapEntryLike
import org.yaml.model.{YMap, YMapEntry}

case class AsyncOperationBindingsParser(entryLike: YMapEntryLike)(implicit ctx: AsyncWebApiContext)
    extends AsyncBindingsParser(entryLike) {
  override type Binding  = OperationBinding
  override type Bindings = OperationBindings
  override val bindingsField: Field = OperationBindingsModel.Bindings

  override protected def createBindings(): OperationBindings = OperationBindings()

  protected def createParser(entryOrMap: YMapEntryLike)(implicit ctx: AsyncWebApiContext): AsyncBindingsParser =
    AsyncOperationBindingsParser(entryOrMap)

  def handleRef(fullRef: String): OperationBindings = {
    val label = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "operationBindings")
    ctx.declarations
      .findOperationBindings(label, SearchScope.Named)
      .map(operationBindings =>
        nameAndAdopt(
          operationBindings.link(AmfScalar(label), Annotations(entryLike.value), Annotations.synthesized()),
          entryLike.key
        )
      )
      .getOrElse(remote(fullRef, entryLike))
  }

  override protected def errorBindings(fullRef: String, entryLike: YMapEntryLike): OperationBindings =
    new ErrorOperationBindings(fullRef, entryLike.asMap)

  override protected def parseHttp(entry: YMapEntry, parent: String)(implicit
      ctx: AsyncWebApiContext
  ): OperationBinding = {
    val binding = HttpOperationBinding(Annotations(entry))
    val map     = entry.value.as[YMap]

    map.key("type", HttpOperationBindingModel.OperationType in binding)
    if (binding.operationType.is("request")) map.key("method", HttpOperationBindingModel.Method in binding)
    map.key("query", entry => parseSchema(HttpOperationBindingModel.Query, binding, entry, binding.id))
    parseBindingVersion(binding, HttpOperationBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "httpOperationBinding")

    binding
  }

  override protected def parseAmqp(entry: YMapEntry, parent: String)(implicit
      ctx: AsyncWebApiContext
  ): OperationBinding = {
    val binding = Amqp091OperationBinding(Annotations(entry))
    val map     = entry.value.as[YMap]

    map.key("expiration", Amqp091OperationBindingModel.Expiration in binding)
    map.key("userId", Amqp091OperationBindingModel.UserId in binding)
    map.key("cc", Amqp091OperationBindingModel.CC in binding)
    map.key("priority", Amqp091OperationBindingModel.Priority in binding)
    map.key("deliveryMode", Amqp091OperationBindingModel.DeliveryMode in binding)
    map.key("mandatory", Amqp091OperationBindingModel.Mandatory in binding)
    map.key("bcc", Amqp091OperationBindingModel.BCC in binding)
    map.key("replyTo", Amqp091OperationBindingModel.ReplyTo in binding)
    map.key("timestamp", Amqp091OperationBindingModel.Timestamp in binding)
    map.key("ack", Amqp091OperationBindingModel.Ack in binding)

    parseBindingVersion(binding, KafkaOperationBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "amqpOperationBinding")

    binding
  }

  override protected def parseKafka(entry: YMapEntry, parent: String)(implicit
      ctx: AsyncWebApiContext
  ): OperationBinding = {
    val binding = KafkaOperationBinding(Annotations(entry))
    val map     = entry.value.as[YMap]

    map.key(
      "groupId",
      entry => parseSchema(KafkaOperationBindingModel.GroupId, binding, entry, binding.id + "/group-id")
    )
    map.key(
      "clientId",
      entry => parseSchema(KafkaOperationBindingModel.ClientId, binding, entry, binding.id + "/client-id")
    )
    parseBindingVersion(binding, KafkaOperationBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "kafkaOperationBinding")

    binding
  }

  override protected def parseMqtt(entry: YMapEntry, parent: String)(implicit
      ctx: AsyncWebApiContext
  ): OperationBinding = {
    val binding = MqttOperationBinding(Annotations(entry))
    val map     = entry.value.as[YMap]

    map.key("qos", MqttOperationBindingModel.Qos in binding)
    map.key("retain", MqttOperationBindingModel.Retain in binding)
    parseBindingVersion(binding, MqttOperationBindingModel.BindingVersion, map)

    ctx.closedShape(binding, map, "mqttOperationBinding")

    binding
  }
}
