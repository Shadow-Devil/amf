package amf.apicontract.internal.metamodel.domain.bindings

import amf.core.client.scala.vocabulary.Namespace.ApiBinding
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.templates.KeyField
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}

trait ChannelBindingModel extends DomainElementModel with BindingType with KeyField

object ChannelBindingModel extends ChannelBindingModel {

  override def modelInstance           = throw new Exception("ChannelBinding is an abstract class")
  override def fields: List[Field]     = List(Type) ++ DomainElementModel.fields
  override val `type`: List[ValueType] = ApiBinding + "ChannelBinding" :: DomainElementModel.`type`

  override val key: Field = Type

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiBinding,
    "ChannelBinding",
    ""
  )
}
