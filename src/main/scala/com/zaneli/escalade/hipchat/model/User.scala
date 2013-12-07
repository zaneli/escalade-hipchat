package com.zaneli.escalade.hipchat.model

import com.zaneli.escalade.hipchat.util.DataHandler
import net.liftweb.json.{ DefaultFormats, JValue }
import org.scala_tools.time.Imports.DateTime

case class User (
    userId: Int, name: String, mentionName: String, email: String, title: String, password: Option[String], photoUrl: String,
    lastActive: Option[DateTime], created: Option[DateTime], status: String, statusMessage: String, timezone: String,
    isGroupAdmin: Boolean, isDeleted: Boolean) {

  override def toString =
    s"User(userId=${userId}, name=${name}, mentionName=${mentionName}, email=${email}, title=${title}, password=${password}, " +
    s"photoUrl=${photoUrl}, lastActive=${lastActive}, created=${created}, status=${status}, statusMessage=${statusMessage}, " +
    s"timezone=${timezone}, isGroupAdmin=${isGroupAdmin}, isDeleted=${isDeleted})"
}

object User extends DataHandler {
  private[this] case class DataHolder(
    user_id: Int, name: String, mention_name: String, email: String, title: String, password: Option[String], photo_url: String,
    last_active: Long, created: Created, status: String, status_message: String, timezone: String, is_group_admin: Int, is_deleted: Int)
  private[User] case class Created(value: Option[Long])

  def apply(value: JValue): User = {
    implicit val formats = DefaultFormats + CreatedSerializer
    val holder = value.extract[DataHolder]
    User(
      holder.user_id, holder.name, holder.mention_name, holder.email, holder.title, empty2None(holder.password),
      holder.photo_url, sec2DateTime(holder.last_active), holder.created.value.map(sec2DateTime).flatten,
      holder.status, holder.status_message, holder.timezone, int2bool(holder.is_group_admin), int2bool(holder.is_deleted))
  }

  import net.liftweb.json._
  /**
   * A helper that will JSON serialize "created"
   * users/create メソッドのレスポンスの json に "created":false　が含まれるため独自に実装
   */
  private[this] object CreatedSerializer extends Serializer[Created] {
    private val Class = classOf[Created]

    def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Created] = {
      case (TypeInfo(Class, _), json) => json match {
        case JInt(iv) => Created(Some(iv.toLong))
        case JBool(bv) => Created(None)
        case value => throw new MappingException("Can't convert " + value + " to " + Class)
      }
    }

    // json => Scala のデシリアライズにしか使用していないため、シリアライズメソッドは未実装
    def serialize(implicit format: Formats): PartialFunction[Any, JValue] = ???
  }
}
