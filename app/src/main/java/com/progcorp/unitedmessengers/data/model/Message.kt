package com.progcorp.unitedmessengers.data.model

import android.util.Log
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.data.model.companions.Bot
import com.progcorp.unitedmessengers.data.model.companions.Chat
import com.progcorp.unitedmessengers.data.model.companions.User
import com.progcorp.unitedmessengers.interfaces.ICompanion
import com.progcorp.unitedmessengers.interfaces.IMessageContent
import com.progcorp.unitedmessengers.util.Constants
import kotlinx.coroutines.flow.*
import org.drinkless.td.libcore.telegram.TdApi
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable

data class Message(
    var id: Long = 0,
    val conversationId: Long = 0,
    var timeStamp: Long = 0,
    val sender: ICompanion? = null,
    val isOutgoing: Boolean = false,
    val replyToMessage: Message? = null,
    val forwardedMessages: List<Message>? = null,
    var content: IMessageContent = MessageText(),
    var canBeEdited: Boolean = false,
    var canBeDeletedOnlyForSelf: Boolean = false,
    var canBeDeletedForAllUsers: Boolean = false,
    var messenger: Int = 0
) : Serializable {

    fun updateMessageContent(content: TdApi.MessageContent) {
        when (content.constructor) {
            TdApi.MessageText.CONSTRUCTOR -> {
                this.content = MessageText((content as TdApi.MessageText).text.text)
            }
            TdApi.MessageAnimation.CONSTRUCTOR -> {
                content as TdApi.MessageAnimation
                val photo = content.animation.animation!!
                this.content = MessageAnimation()
            }
            TdApi.MessageAudio.CONSTRUCTOR -> {
                content as TdApi.MessageAudio
                this.content = MessageUnknown(content.caption.text, content.audio.fileName)
            }
            TdApi.MessageDocument.CONSTRUCTOR -> {
                content as TdApi.MessageDocument
                this.content = MessageUnknown(content.caption.text, content.document.fileName)
            }
            TdApi.MessagePhoto.CONSTRUCTOR -> {
                content as TdApi.MessagePhoto
                val path: String
                val photoId: Int
                val photoWidth: Int
                val photoHeight: Int
                content.photo.sizes[content.photo.sizes.size - 1].let {
                    photoId = it.photo.id
                    photoHeight = it.height
                    photoWidth = it.width
                    path = if (it.photo.local.isDownloadingCompleted){
                        it.photo.local.path
                    } else {
                        it.photo.id.toString()
                    }
                }
                val photoObj = Photo(photoId.toLong(), photoWidth, photoHeight, path)
                photoObj.adaptToChatSize()
                this.content = MessagePhoto(content.caption.text, photoObj)
            }
            TdApi.MessageExpiredPhoto.CONSTRUCTOR -> {
                this.content = MessageExpiredPhoto()
            }
            TdApi.MessageSticker.CONSTRUCTOR -> {
                content as TdApi.MessageSticker
                this.content = if (content.sticker.isAnimated) {
                    MessageText(text = content.sticker.emoji)
                } else {
                    if (content.sticker.sticker.local.isDownloadingCompleted) {
                        MessageSticker(path = content.sticker.sticker.local.path)
                    }
                    else {
                        MessageSticker(path = content.sticker.sticker.id.toString())
                    }
                }
            }
            TdApi.MessageVideo.CONSTRUCTOR -> {
                content as TdApi.MessageVideo
                content.video.thumbnail?.let {
                    val photoObj = Photo(
                        it.file.id.toLong(),
                        it.width,
                        it.height
                    )
                    photoObj.adaptToChatSize()
                    this.content = if (it.file.local.isDownloadingCompleted) {
                        photoObj.path = it.file.local.path
                        MessagePhoto(
                            text = content.caption.text,
                            photo = photoObj
                        )
                    }
                    else {
                        photoObj.path = it.file.id.toString()
                        MessagePhoto(
                            text = content.caption.text,
                            photo = photoObj
                        )
                    }
                }
            }
            TdApi.MessageExpiredVideo.CONSTRUCTOR -> {
                this.content = MessageExpiredVideo()
            }
            TdApi.MessageVideoNote.CONSTRUCTOR -> {
                content as TdApi.MessageVideoNote
                content.videoNote.thumbnail?.let {
                    val photoObj = Photo(
                        it.file.id.toLong(),
                        it.width,
                        it.height
                    )
                    photoObj.adaptToChatSize()
                    this.content = if (it.file.local.isDownloadingCompleted) {
                        photoObj.path = it.file.local.path
                        MessagePhoto(
                            photo = photoObj
                        )
                    }
                    else {
                        photoObj.path = it.file.id.toString()
                        MessagePhoto(
                            photo = photoObj
                        )
                    }
                }
            }
            TdApi.MessageVoiceNote.CONSTRUCTOR -> {
                content as TdApi.MessageVoiceNote
                this.content = MessageVoiceNote(content.caption.text)
            }
            TdApi.MessageLocation.CONSTRUCTOR -> {
                this.content = MessageLocation()
            }
            TdApi.MessageVenue.CONSTRUCTOR -> {
                this.content = MessageLocation()
            }
            TdApi.MessageContact.CONSTRUCTOR -> {
                this.content = MessageUnknown(info = "??????????????")
            }
            TdApi.MessageAnimatedEmoji.CONSTRUCTOR -> {
                content as TdApi.MessageAnimatedEmoji
                this.content = MessageText(content.emoji)
            }
            TdApi.MessageDice.CONSTRUCTOR -> {
                content as TdApi.MessageDice
                this.content = MessageUnknown(content.emoji, content.value.toString())
            }
            TdApi.MessageGame.CONSTRUCTOR -> {
                this.content = MessageUnknown(info = "????????")
            }
            TdApi.MessagePoll.CONSTRUCTOR -> {
                this.content = MessagePoll()
            }
            TdApi.MessageInvoice.CONSTRUCTOR -> {
                this.content = MessageUnknown(info = "????????")
            }
            TdApi.MessageCall.CONSTRUCTOR -> {
                this.content = MessageUnknown(info = "????????????")
            }
            TdApi.MessageVideoChatScheduled.CONSTRUCTOR -> {
                this.content = MessageUnknown(info = "?????????????????????????????? ??????????????????????")
            }
            TdApi.MessageVideoChatStarted.CONSTRUCTOR -> {
                this.content = MessageUnknown(info = "?????????????????????? ??????????")
            }
            TdApi.MessageVideoChatEnded.CONSTRUCTOR -> {
                this.content = MessageUnknown(info = "?????????????????????? ??????????????")
            }
            TdApi.MessageInviteVideoChatParticipants.CONSTRUCTOR -> {
                this.content = MessageUnknown(info = "?????????????????????? ?? ??????????????????????")
            }
            TdApi.MessageChatChangeTitle.CONSTRUCTOR -> {
                this.content = MessageChat("?????? ???????????? ????????????????")
            }
            TdApi.MessageChatChangePhoto.CONSTRUCTOR -> {
                this.content = MessageChat("?????? ???????????? ????????")
            }
            TdApi.MessageChatDeletePhoto.CONSTRUCTOR -> {
                this.content = MessageChat("?????? ???????????? ????????")
            }
            TdApi.MessageChatAddMembers.CONSTRUCTOR -> {
                this.content = MessageChat("?????????? ????????????????")
            }
            TdApi.MessageChatJoinByLink.CONSTRUCTOR -> {
                this.content = MessageChat("?????????? ???????????????? ?????????????????????????? ???? ????????????")
            }
            TdApi.MessageChatJoinByRequest.CONSTRUCTOR -> {
                this.content = MessageChat("?????????? ????????????????")
            }
            TdApi.MessageChatDeleteMember.CONSTRUCTOR -> {
                this.content = MessageChat("???????????????? ?????????????? ??????")
            }
            TdApi.MessageChatUpgradeTo.CONSTRUCTOR -> {
                this.content = MessageChat("???????????? ?????????? ???????????????????????? (?)")
            }
            TdApi.MessageChatUpgradeFrom.CONSTRUCTOR -> {
                this.content = MessageChat("???????????? ?????????? ???????????????????????? (?)")
            }
            TdApi.MessagePinMessage.CONSTRUCTOR -> {
                this.content = MessageChat("???????????????????? ??????????????????")
            }
            TdApi.MessageScreenshotTaken.CONSTRUCTOR -> {
                this.content = MessageChat("?????? ???????????? ???????????????? ????????")
            }
            TdApi.MessageChatSetTheme.CONSTRUCTOR -> {
                this.content = MessageChat("???????????????? ???????? ????????")
            }
            TdApi.MessageChatSetTtl.CONSTRUCTOR -> {
                this.content = MessageChat("?????????? ?????????? ?????????????????? ????????????????")
            }
            TdApi.MessageCustomServiceAction.CONSTRUCTOR -> {
                this.content = MessageChat("??????-???? ??????????????????")
            }
            TdApi.MessageGameScore.CONSTRUCTOR -> {
                this.content = MessageChat("?? ???????? ?????????? ????????????")
            }
            TdApi.MessagePaymentSuccessful.CONSTRUCTOR -> {
                this.content = MessageChat("???????????????? ????????????")
            }
            TdApi.MessagePaymentSuccessfulBot.CONSTRUCTOR -> {
                this.content = MessageChat("???????????????? ????????????")
            }
            TdApi.MessageContactRegistered.CONSTRUCTOR -> {
                this.content = MessageChat("?????????????????????????? ?? Telegram")
            }
            else -> {
                this.content = MessageUnknown(info = "??????????-???? ????????????????")
            }
        }
    }
    companion object {
        suspend fun vkParse(json: JSONObject, profiles: JSONArray?, groups: JSONArray?): Message {
            val id = json.optLong("id")
            val conversationId = json.optLong("peer_id")
            val timeStamp = json.optLong("date") * 1000

            var sender: ICompanion? = null
            if (profiles != null) {
                for (i in 0 until profiles.length()) {
                    val profile = profiles.getJSONObject(i)
                    if (profile.getLong("id") == json.getLong("from_id")) {
                        sender = User.vkParse(profile)
                        break
                    }
                }
            }
            if (sender == null && groups != null) {
                for (i in 0 until groups.length()) {
                    val group = groups.getJSONObject(i)
                    if (group.getLong("id") == -json.getLong("from_id")) {
                        sender = Bot.vkParse(group)
                        break
                    }
                }
            }
            if (sender == null) {
                sender = json.optJSONObject("chat_settings")?.let {
                    Chat.vkParse(it, id)
                }
            }

            val isOutgoing: Boolean = json.optInt("out") == 1
            val replyToMessage: Message? =
                json.optJSONObject("reply_message")?.let { vkParse(it, profiles, groups) }

            val text = json.getString("text")
            var messageContent: IMessageContent = MessageText(text)

            if (sender is Chat) {
                val actionObject = json.optJSONObject("action")
                if (actionObject != null) {
                    val action = when (actionObject.getString("type")) {
                        "chat_photo_update" -> "?????????????????? ???????????????????? ????????????"
                        "chat_photo_remove " -> "?????????????? ???????????????????? ????????????"
                        "chat_create" -> "?????????????? ????????????"
                        "chat_title_update" -> "?????????????????? ???????????????? ????????????"
                        "chat_invite_user" -> "?????????????????? ????????????????????????"
                        "chat_kick_user" -> "???????????????? ????????????????????????"
                        "chat_pin_message" -> "???????????????????? ??????????????????"
                        "chat_unpin_message" -> "???????????????????? ??????????????????"
                        "chat_invite_user_by_link" -> "???????????????????????? ??????????????????????????"
                        else -> "???????????????? ?? ????????????"
                    }
                    messageContent = MessageChat(action)
                }
            }

            try {
                val attachmentsObject = json.getJSONArray("attachments")
                if (attachmentsObject.length() == 1) {
                    val at = attachmentsObject.getJSONObject(0)
                    when (at.getString("type")) {
                        "sticker" -> {
                            val stickers = at.getJSONObject("sticker")
                                .getJSONArray("images")
                            val sticker: String = if (stickers.length() == 5) {
                                stickers
                                    .getJSONObject(4)
                                    .getString("url")
                            } else {
                                stickers
                                    .getJSONObject(1)
                                    .getString("url")
                            }
                            messageContent = MessageSticker(path = sticker)
                        }
                        "photo" -> {
                            val photoArray = at.getJSONObject("photo").getJSONArray("sizes")
                            for (i in 0 until photoArray.length()) {
                                val photo = photoArray.getJSONObject(i)
                                if (photo.getString("type") == "y" || photo.getString("type") == "x") {
                                    val photoObj = Photo(
                                        id = at.getJSONObject("photo").getLong("id"),
                                        width = photo.getInt("width"),
                                        height = photo.getInt("height"),
                                        path = photo.getString("url")
                                    )
                                    photoObj.adaptToChatSize()
                                    messageContent = MessagePhoto(text, photoObj)
                                    break
                                }
                            }
                        }
                        "video" -> {
                            val video = at.getJSONObject("video")
                                .getJSONArray("image")
                                .getJSONObject(3)
                                .getString("url")
                            messageContent = MessageVideo(text, video)
                        }
                        "audio" -> {
                            val audioObj = at.getJSONObject("audio")
                            val name = audioObj.getString("artist") +
                                    " " + audioObj.getString("title")
                            messageContent = MessageUnknown(text, name)
                        }
                        "audio_message" -> {
                            val audio = at.getJSONObject("audio_message")
                                .getString("link_mp3")
                            messageContent = MessageVoiceNote(text, audio)

                        }
                        "doc" -> {
                            val doc = at.getJSONObject("doc")
                                .getString("title")
                            messageContent = MessageDocument(text, doc)
                        }
                        "link" -> {}
                        "market" -> {
                            messageContent = MessageUnknown(text, "??????????")
                        }
                        "market_album" -> {
                            messageContent = MessageUnknown(text, "???????????????? ??????????????")
                        }
                        "wall" -> {
                            messageContent = MessageUnknown(text, "???????????? ???? ??????????")
                        }
                        "wall_reply" -> {
                            messageContent = MessageUnknown(text, "?????????????????????? ?? ????????????")
                        }
                        "gift" -> {
                            messageContent = MessageUnknown(text, "??????????????")
                        }
                    }
                } else if (attachmentsObject.length() > 1) {
                    var items = arrayListOf<String>()
                    for (i in 0 until attachmentsObject.length()) {
                        val at = attachmentsObject.getJSONObject(i)
                        when (at.getString("type")) {
                            "photo" -> {
                                items.add(
                                    at.getJSONObject("photo")
                                        .getJSONArray("sizes")
                                        .getJSONObject(4)
                                        .getString("url")
                                )
                            }
                            "video" -> {
                                items.add(
                                    at.getJSONObject("video")
                                        .getJSONArray("image")
                                        .getJSONObject(5)
                                        .getString("url")
                                )
                            }
                            else -> {
                                items = arrayListOf()
                                break
                            }
                        }
                    }
                    messageContent = if (items.size > 0) {
                        MessageCollage(text, items)
                    } else {
                        MessageUnknown(text, "????????????????")
                    }
                }
            } catch (ex: JSONException) {
                Log.e("Message.vkParse", ex.stackTraceToString())
                messageContent = MessageUnknown("???????????????????????????? ??????????????????")
            }

            val currentTimestamp = System.currentTimeMillis()
            val canBeEdited = currentTimestamp - timeStamp < 86400000 && isOutgoing

            return Message(
                id = id,
                conversationId = conversationId,
                timeStamp = timeStamp,
                sender = sender,
                isOutgoing = isOutgoing,
                replyToMessage = replyToMessage,
                forwardedMessages = null,
                content = messageContent,
                canBeEdited = canBeEdited,
                canBeDeletedForAllUsers = canBeEdited,
                canBeDeletedOnlyForSelf = true,
                messenger = Constants.Messenger.VK
            )
        }

        suspend fun tgParse(tgMessage: TdApi.Message): Message {
            val repository = App.application.tgClient.repository

            val id = tgMessage.id
            val timeStamp: Long = (tgMessage.date).toLong() * 1000

            val sender: ICompanion = when (tgMessage.senderId.constructor) {
                TdApi.MessageSenderUser.CONSTRUCTOR -> {
                    User.tgParse(repository.getUser((tgMessage.senderId as TdApi.MessageSenderUser).userId).first())
                }
                else -> {
                    val conversation = repository.getConversation((tgMessage.senderId as TdApi.MessageSenderChat).chatId).first()
                    when(conversation.type.constructor) {
                        TdApi.ChatTypeBasicGroup.CONSTRUCTOR -> {
                            Chat.tgParseBasicGroup(conversation, repository.getBasicGroup((conversation.type as TdApi.ChatTypeBasicGroup).basicGroupId).first())
                        }
                        TdApi.ChatTypeSupergroup.CONSTRUCTOR -> {
                            Chat.tgParseSupergroup(conversation, repository.getSupergroup((conversation.type as TdApi.ChatTypeSupergroup).supergroupId).first())
                        }
                        else -> {
                            Chat()
                        }
                    }
                }
            }

            val isOutgoing = tgMessage.isOutgoing

            val replyToMessage: Message? = if (tgMessage.replyToMessageId != 0.toLong()) {
                repository.getMessage(tgMessage.chatId, tgMessage.replyToMessageId).first()
                    ?.let { tgParse(it) }
            } else {
                null
            }


            var messageContent: IMessageContent = MessageText()

            when (tgMessage.content.constructor) {
                TdApi.MessageText.CONSTRUCTOR -> {
                    messageContent = MessageText((tgMessage.content as TdApi.MessageText).text.text)
                }
                TdApi.MessageAnimation.CONSTRUCTOR -> {
                    val content = tgMessage.content as TdApi.MessageAnimation
                    val photo = content.animation.animation!!
                    messageContent = MessageAnimation()
                }
                TdApi.MessageAudio.CONSTRUCTOR -> {
                    val content = tgMessage.content as TdApi.MessageAudio
                    messageContent = MessageUnknown(content.caption.text, content.audio.fileName)
                }
                TdApi.MessageDocument.CONSTRUCTOR -> {
                    val content = tgMessage.content as TdApi.MessageDocument
                    messageContent = MessageUnknown(content.caption.text, content.document.fileName)
                }
                TdApi.MessagePhoto.CONSTRUCTOR -> {
                    val content = tgMessage.content as TdApi.MessagePhoto
                    val path: String
                    val photoId: Int
                    val photoWidth: Int
                    val photoHeight: Int
                    content.photo.sizes[content.photo.sizes.size - 1].let {
                        photoId = it.photo.id
                        photoHeight = it.height
                        photoWidth = it.width
                        path = if (it.photo.local.isDownloadingCompleted){
                            it.photo.local.path
                        } else {
                            it.photo.id.toString()
                        }
                    }
                    val photoObj = Photo(photoId.toLong(), photoWidth, photoHeight, path)
                    photoObj.adaptToChatSize()
                    messageContent = MessagePhoto(content.caption.text, photoObj)
                }
                TdApi.MessageExpiredPhoto.CONSTRUCTOR -> {
                    messageContent = MessageExpiredPhoto()
                }
                TdApi.MessageSticker.CONSTRUCTOR -> {
                    val content = tgMessage.content as TdApi.MessageSticker
                    messageContent = if (content.sticker.isAnimated) {
                        MessageText(text = content.sticker.emoji)
                    } else {
                        if (content.sticker.sticker.local.isDownloadingCompleted) {
                            MessageSticker(path = content.sticker.sticker.local.path)
                        }
                        else {
                            MessageSticker(path = content.sticker.sticker.id.toString())
                        }
                    }
                }
                TdApi.MessageVideo.CONSTRUCTOR -> {
                    val content = tgMessage.content as TdApi.MessageVideo
                    content.video.thumbnail?.let {
                        val photoObj = Photo(
                            it.file.id.toLong(),
                            it.width,
                            it.height
                        )
                        photoObj.adaptToChatSize()
                        messageContent = if (it.file.local.isDownloadingCompleted) {
                            photoObj.path = it.file.local.path
                            MessagePhoto(
                                text = content.caption.text,
                                photo = photoObj
                            )
                        }
                        else {
                            photoObj.path = it.file.id.toString()
                            MessagePhoto(
                                text = content.caption.text,
                                photo = photoObj
                            )
                        }
                    }
                }
                TdApi.MessageExpiredVideo.CONSTRUCTOR -> {
                    messageContent = MessageExpiredVideo()
                }
                TdApi.MessageVideoNote.CONSTRUCTOR -> {
                    val content = tgMessage.content as TdApi.MessageVideoNote
                    content.videoNote.thumbnail?.let {
                        val photoObj = Photo(
                            it.file.id.toLong(),
                            it.width,
                            it.height
                        )
                        photoObj.adaptToChatSize()
                        messageContent = if (it.file.local.isDownloadingCompleted) {
                            photoObj.path = it.file.local.path
                            MessagePhoto(
                                photo = photoObj
                            )
                        }
                        else {
                            photoObj.path = it.file.id.toString()
                            MessagePhoto(
                                photo = photoObj
                            )
                        }
                    }
                }
                TdApi.MessageVoiceNote.CONSTRUCTOR -> {
                    val content = tgMessage.content as TdApi.MessageVoiceNote
                    messageContent = MessageVoiceNote(content.caption.text)
                }
                TdApi.MessageLocation.CONSTRUCTOR -> {
                    messageContent = MessageLocation()
                }
                TdApi.MessageVenue.CONSTRUCTOR -> {
                    messageContent = MessageLocation()
                }
                TdApi.MessageContact.CONSTRUCTOR -> {
                    messageContent = MessageUnknown(info = "??????????????")
                }
                TdApi.MessageAnimatedEmoji.CONSTRUCTOR -> {
                    val content = tgMessage.content as TdApi.MessageAnimatedEmoji
                    messageContent = MessageText(content.emoji)
                }
                TdApi.MessageDice.CONSTRUCTOR -> {
                    val content = tgMessage.content as TdApi.MessageDice
                    messageContent = MessageUnknown(content.emoji, content.value.toString())
                }
                TdApi.MessageGame.CONSTRUCTOR -> {
                    messageContent = MessageUnknown(info = "????????")
                }
                TdApi.MessagePoll.CONSTRUCTOR -> {
                    messageContent = MessagePoll()
                }
                TdApi.MessageInvoice.CONSTRUCTOR -> {
                    messageContent = MessageUnknown(info = "????????")
                }
                TdApi.MessageCall.CONSTRUCTOR -> {
                    messageContent = MessageUnknown(info = "????????????")
                }
                TdApi.MessageVideoChatScheduled.CONSTRUCTOR -> {
                    messageContent = MessageUnknown(info = "?????????????????????????????? ??????????????????????")
                }
                TdApi.MessageVideoChatStarted.CONSTRUCTOR -> {
                    messageContent = MessageUnknown(info = "?????????????????????? ??????????")
                }
                TdApi.MessageVideoChatEnded.CONSTRUCTOR -> {
                    messageContent = MessageUnknown(info = "?????????????????????? ??????????????")
                }
                TdApi.MessageInviteVideoChatParticipants.CONSTRUCTOR -> {
                    messageContent = MessageUnknown(info = "?????????????????????? ?? ??????????????????????")
                }
                TdApi.MessageChatChangeTitle.CONSTRUCTOR -> {
                    messageContent = MessageChat("?????? ???????????? ????????????????")
                }
                TdApi.MessageChatChangePhoto.CONSTRUCTOR -> {
                    messageContent = MessageChat("?????? ???????????? ????????")
                }
                TdApi.MessageChatDeletePhoto.CONSTRUCTOR -> {
                    messageContent = MessageChat("?????? ???????????? ????????")
                }
                TdApi.MessageChatAddMembers.CONSTRUCTOR -> {
                    messageContent = MessageChat("?????????? ????????????????")
                }
                TdApi.MessageChatJoinByLink.CONSTRUCTOR -> {
                    messageContent = MessageChat("?????????? ???????????????? ?????????????????????????? ???? ????????????")
                }
                TdApi.MessageChatJoinByRequest.CONSTRUCTOR -> {
                    messageContent = MessageChat("?????????? ????????????????")
                }
                TdApi.MessageChatDeleteMember.CONSTRUCTOR -> {
                    messageContent = MessageChat("???????????????? ?????????????? ??????")
                }
                TdApi.MessageChatUpgradeTo.CONSTRUCTOR -> {
                    messageContent = MessageChat("???????????? ?????????? ???????????????????????? (?)")
                }
                TdApi.MessageChatUpgradeFrom.CONSTRUCTOR -> {
                    messageContent = MessageChat("???????????? ?????????? ???????????????????????? (?)")
                }
                TdApi.MessagePinMessage.CONSTRUCTOR -> {
                    messageContent = MessageChat("???????????????????? ??????????????????")
                }
                TdApi.MessageScreenshotTaken.CONSTRUCTOR -> {
                    messageContent = MessageChat("?????? ???????????? ???????????????? ????????")
                }
                TdApi.MessageChatSetTheme.CONSTRUCTOR -> {
                    messageContent = MessageChat("???????????????? ???????? ????????")
                }
                TdApi.MessageChatSetTtl.CONSTRUCTOR -> {
                    messageContent = MessageChat("?????????? ?????????? ?????????????????? ????????????????")
                }
                TdApi.MessageCustomServiceAction.CONSTRUCTOR -> {
                    messageContent = MessageChat("??????-???? ??????????????????")
                }
                TdApi.MessageGameScore.CONSTRUCTOR -> {
                    messageContent = MessageChat("?? ???????? ?????????? ????????????")
                }
                TdApi.MessagePaymentSuccessful.CONSTRUCTOR -> {
                    messageContent = MessageChat("???????????????? ????????????")
                }
                TdApi.MessagePaymentSuccessfulBot.CONSTRUCTOR -> {
                    messageContent = MessageChat("???????????????? ????????????")
                }
                TdApi.MessageContactRegistered.CONSTRUCTOR -> {
                    messageContent = MessageChat("?????????????????????????? ?? Telegram")
                }
                else -> {
                    messageContent = MessageUnknown(info = "??????????-???? ????????????????")
                }
            }

            return Message(
                id = id,
                conversationId = tgMessage.chatId,
                timeStamp = timeStamp,
                sender = sender,
                isOutgoing = isOutgoing,
                replyToMessage = replyToMessage,
                forwardedMessages = null,
                content = messageContent,
                canBeEdited = tgMessage.canBeEdited,
                canBeDeletedOnlyForSelf = tgMessage.canBeDeletedOnlyForSelf,
                canBeDeletedForAllUsers = tgMessage.canBeDeletedForAllUsers,
                messenger = Constants.Messenger.TG
            )
        }
    }
}