package com.zaneli.escalade.hipchat.model

import com.zaneli.escalade.hipchat.util.DataHandler
import org.json4s.{ DefaultFormats, JValue }

case class UserIdentifier(userId: Either[String, Long], name: String) {
  override def toString = s"UserIdentifier(userId=${userId}, name=${name})"
}

object UserIdentifier {
  private[model] case class DataHolder(user_id: UserId, name: String)
  private[UserIdentifier] case class UserId(value: Either[String, Long])

  def apply(holder: DataHolder): UserIdentifier = {
    UserIdentifier(holder.user_id.value, holder.name)
  }

  import org.json4s._
  /**
   * A helper that will JSON deserialize UserId
   * "user_id" の値は通常数値だが、"api", "guest" が文字列として設定される場合があるため独自に実装
   */
  private[model] object UserIdSerializer extends Serializer[UserId] {
    private val Class = classOf[UserId]

    def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), UserId] = {
      case (TypeInfo(Class, _), json) => json match {
        case JInt(iv) => UserId(Right(iv.toInt))
        case JString(sv) => UserId(Left(sv))
        case value => throw new MappingException("Can't convert " + value + " to " + Class)
      }
    }

    /** json => Scala のデシリアライズにしか使用していないため、シリアライズメソッドは未実装 */
    def serialize(implicit format: Formats): PartialFunction[Any, JValue] = ???
  }
}
