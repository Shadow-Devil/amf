package amf.apicontract.internal.spec.async.emitters.bindings

import amf.apicontract.client.scala.model.domain.bindings.MessageBinding
import amf.apicontract.client.scala.model.domain.bindings.amqp.Amqp091MessageBinding
import amf.apicontract.client.scala.model.domain.bindings.http.HttpMessageBinding
import amf.apicontract.client.scala.model.domain.bindings.kafka.KafkaMessageBinding
import amf.apicontract.client.scala.model.domain.bindings.mqtt.MqttMessageBinding
import amf.apicontract.internal.metamodel.domain.bindings.{
  Amqp091MessageBindingModel,
  HttpMessageBindingModel,
  KafkaMessageBindingModel
}
import amf.apicontract.internal.spec.async.emitters.domain
import amf.apicontract.internal.spec.oas.emitter.context.OasLikeSpecEmitterContext
import org.mulesoft.common.client.lexical.Position
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.render.BaseEmitters.{ValueEmitter, pos, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import org.yaml.model.YDocument.EntryBuilder
import org.yaml.model.{YDocument, YNode}

import scala.collection.mutable.ListBuffer

class AsyncApiMessageBindingsEmitter(binding: MessageBinding, ordering: SpecOrdering)(implicit
    val spec: OasLikeSpecEmitterContext
) extends EntryEmitter {

  override def emit(b: YDocument.EntryBuilder): Unit = {
    emitterFor(binding).foreach(emitter => emitter.emit(b))
  }

  private def emitterFor(binding: MessageBinding): Option[EntryEmitter] = binding match {
    case binding: Amqp091MessageBinding => Some(new Amqp091MessageEmitter(binding, ordering))
    case binding: HttpMessageBinding    => Some(new HttpMessageEmitter(binding, ordering))
    case binding: KafkaMessageBinding   => Some(new KafkaMessageEmitter(binding, ordering))
    case binding: MqttMessageBinding    => Some(new MqttMessageEmitter(binding, ordering))
    case _                              => None
  }

  override def position(): Position = pos(binding.annotations)
}

class HttpMessageEmitter(binding: HttpMessageBinding, ordering: SpecOrdering)(implicit
    val spec: OasLikeSpecEmitterContext
) extends AsyncApiCommonBindingEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      YNode("http"),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = binding.fields

        fs.entry(HttpMessageBindingModel.Headers)
          .foreach(f => result += domain.AsyncSchemaEmitter("headers", f.element.asInstanceOf[Shape], ordering, Seq()))
        emitBindingVersion(fs, result)

        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  override def position(): Position = pos(binding.annotations)
}

class KafkaMessageEmitter(binding: KafkaMessageBinding, ordering: SpecOrdering)(implicit
    val spec: OasLikeSpecEmitterContext
) extends AsyncApiCommonBindingEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      YNode("kafka"),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = binding.fields

        fs.entry(KafkaMessageBindingModel.MessageKey)
          .foreach(f => result += domain.AsyncSchemaEmitter("key", f.element.asInstanceOf[Shape], ordering, Seq()))
        emitBindingVersion(fs, result)
        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  override def position(): Position = pos(binding.annotations)
}

class MqttMessageEmitter(binding: MqttMessageBinding, ordering: SpecOrdering)(implicit
    val spec: OasLikeSpecEmitterContext
) extends AsyncApiCommonBindingEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      YNode("mqtt"),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = binding.fields
        emitBindingVersion(fs, result)
        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  override def position(): Position = pos(binding.annotations)
}

class Amqp091MessageEmitter(binding: Amqp091MessageBinding, ordering: SpecOrdering)(implicit
    val spec: OasLikeSpecEmitterContext
) extends AsyncApiCommonBindingEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      YNode("amqp"),
      _.obj { emitter =>
        val result = ListBuffer[EntryEmitter]()
        val fs     = binding.fields
        fs.entry(Amqp091MessageBindingModel.ContentEncoding).foreach(f => result += ValueEmitter("contentEncoding", f))
        fs.entry(Amqp091MessageBindingModel.MessageType).foreach(f => result += ValueEmitter("messageType", f))
        emitBindingVersion(fs, result)
        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  override def position(): Position = pos(binding.annotations)
}
