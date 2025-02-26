package amf.apicontract.internal.validation.shacl.graphql.values

import amf.core.client.scala.model.domain.{DataNode, ScalarNode}
import amf.core.internal.metamodel.Field
import amf.shapes.client.scala.model.domain.{NilShape, UnionShape}
import amf.validation.internal.shacl.custom.CustomShaclValidator.ValidationInfo

object NullableValueValidator extends ValueValidator[UnionShape] {
  override def validate(shape: UnionShape, value: DataNode)(implicit targetField: Field): Seq[ValidationInfo] = {
    value match {
      case s: ScalarNode if isNull(s) => Nil
      case value                      => validateNonNullValue(shape, value)
    }
  }

  private def validateNonNullValue(shape: UnionShape, other: DataNode)(implicit targetField: Field): Seq[ValidationInfo] = {
    val concreteShape = shape.anyOf.filter(!_.isInstanceOf[NilShape]).head
    ValueValidator.validate(concreteShape, other)
  }

}
