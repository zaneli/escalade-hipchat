package com.zaneli.escalade.hipchat.model

import com.github.nscala_time.time.Imports.DateTime
import com.zaneli.escalade.hipchat.util.DataHandler
import org.json4s.{ DefaultFormats, JValue }

case class Room(
    roomId: Int, name: String, topic: String, lastActive: Option[DateTime], created: DateTime, ownerUserId: Int,
    isArchived: Boolean, isPrivate: Boolean, privacy: Option[String], xmppJid: String,
    memberUserIds: List[Int], participants: List[UserIdentifier], guestAccessUrl: Option[String]
) {

  override def toString =
    s"Room(roomId=$roomId, name=$name, topic=$topic, lastActive=$lastActive, created=$created, " +
      s"ownerUserId=$ownerUserId, isArchived=$isArchived, isPrivate=$isPrivate, privacy=$privacy, " +
      s"xmppJid=$xmppJid, memberUserIds=$memberUserIds, participants=$participants, guestAccessUrl=$guestAccessUrl)"
}

object Room extends DataHandler {
  private[this] case class DataHolder(
    room_id: Int, name: String, topic: String, last_active: Long, created: Long, owner_user_id: Int,
    is_archived: Boolean, is_private: Boolean, privacy: Option[String], xmpp_jid: String,
    member_user_ids: List[Int], participants: List[UserIdentifier.DataHolder], guest_access_url: Option[String]
  )

  def apply(value: JValue): Room = {
    implicit val formats = DefaultFormats + UserIdentifier.UserIdSerializer
    val holder = value.extract[DataHolder]
    Room(
      holder.room_id, holder.name, holder.topic, sec2DateTime(holder.last_active), sec2DateTime(holder.created).get,
      holder.owner_user_id, holder.is_archived, holder.is_private, empty2None(holder.privacy), holder.xmpp_jid,
      holder.member_user_ids, holder.participants.map(x => UserIdentifier(x)), empty2None(holder.guest_access_url)
    )
  }
}
