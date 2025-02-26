package amf.apicontract.internal.metamodel.domain.bindings

import amf.core.client.scala.vocabulary.Namespace.ApiBinding
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.templates.KeyField
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}

trait OperationBindingModel extends DomainElementModel with BindingType with KeyField

object OperationBindingModel extends OperationBindingModel {

  override def modelInstance           = throw new Exception("OperationBinding is an abstract class")
  override def fields: List[Field]     = List(Type) ++ DomainElementModel.fields
  override val `type`: List[ValueType] = ApiBinding + "OperationBinding" :: DomainElementModel.`type`

  override val key: Field = Type

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiBinding,
    "OperationBinding",
    ""
  )
}
